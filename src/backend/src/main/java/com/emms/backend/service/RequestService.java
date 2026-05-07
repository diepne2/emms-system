package com.emms.backend.service;

import com.emms.backend.dto.request.RequestCreateResponseDTO;
import com.emms.backend.dto.request.RequestDTO;
import com.emms.backend.entity.Asset;
import com.emms.backend.entity.Location;
import com.emms.backend.entity.Request;
import com.emms.backend.entity.User;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.entity.enums.AssetStatus;
import com.emms.backend.exception.CustomException;
import com.emms.backend.repository.AssetRepository;
import com.emms.backend.repository.LocationRepository;
import com.emms.backend.repository.RequestRepository;
import com.emms.backend.repository.WorkOrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RequestService {

    private final RequestRepository requestRepository;
    private final LocationRepository locationRepository;
    private final AssetRepository assetRepository;
    private final WorkOrderRepository workOrderRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    public RequestService(RequestRepository requestRepository,
                          LocationRepository locationRepository,
                          AssetRepository assetRepository,
                          WorkOrderRepository workOrderRepository,
                          NotificationService notificationService,
                          UserService userService) {
        this.requestRepository = requestRepository;
        this.locationRepository = locationRepository;
        this.assetRepository = assetRepository;
        this.workOrderRepository = workOrderRepository;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<Request> findAll() {
        return requestRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Request findEntityById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() ->
                        new CustomException(
                                "Không tìm thấy request với id: " + id,
                                HttpStatus.NOT_FOUND
                        ));
    }

    public RequestCreateResponseDTO create(RequestDTO dto) {
        Request entity = new Request();

        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setDueDate(dto.getDueDate());
        entity.setPriority(dto.getPriority() != null ? dto.getPriority() : Request.Priority.NONE);
        entity.setStatus(Request.Status.PENDING);
        entity.setCancelled(false);

        if (dto.getLocationId() != null) {
            Location location = locationRepository.findById(dto.getLocationId())
                    .orElseThrow(() ->
                            new CustomException(
                                    "Không tìm thấy location với id: " + dto.getLocationId(),
                                    HttpStatus.NOT_FOUND
                            ));
            entity.setLocation(location);
        } else {
            entity.setLocation(null);
        }

        if (dto.getAssetId() != null) {
            Asset asset = assetRepository.findById(dto.getAssetId())
                    .orElseThrow(() ->
                            new CustomException(
                                    "Không tìm thấy asset với id: " + dto.getAssetId(),
                                    HttpStatus.NOT_FOUND
                            ));
            entity.setAsset(asset);
        } else {
            entity.setAsset(null);
        }

        entity = requestRepository.save(entity);

        notifyCurrentUser(
                "Request đã được tạo",
                "Request \"" + safeTitle(entity.getTitle()) + "\" đã được tạo và đang chờ duyệt."
        );

        return new RequestCreateResponseDTO(
                entity.getId(),
                entity.getStatus().name(),
                null,
                "Tạo request thành công"
        );
    }

    public Request update(Long id, RequestDTO dto) {
        Request entity = findEntityById(id);

        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setDueDate(dto.getDueDate());

        if (dto.getPriority() != null) {
            entity.setPriority(dto.getPriority());
        }

        if (dto.getLocationId() != null) {
            Location location = locationRepository.findById(dto.getLocationId())
                    .orElseThrow(() ->
                            new CustomException(
                                    "Không tìm thấy location với id: " + dto.getLocationId(),
                                    HttpStatus.NOT_FOUND
                            ));
            entity.setLocation(location);
        } else {
            entity.setLocation(null);
        }

        if (dto.getAssetId() != null) {
            Asset asset = assetRepository.findById(dto.getAssetId())
                    .orElseThrow(() ->
                            new CustomException(
                                    "Không tìm thấy asset với id: " + dto.getAssetId(),
                                    HttpStatus.NOT_FOUND
                            ));
            entity.setAsset(asset);
        } else {
            entity.setAsset(null);
        }

        Request saved = requestRepository.save(entity);

        notifyCurrentUser(
                "Request đã cập nhật",
                "Request \"" + safeTitle(saved.getTitle()) + "\" vừa được cập nhật."
        );

        return saved;
    }

    public Request approve(Long id) {
        Request request = findEntityById(id);

        if (request.getStatus() == Request.Status.REJECTED) {
            throw new CustomException("Request đã bị reject, không thể approve", HttpStatus.BAD_REQUEST);
        }

        if (request.getStatus() == Request.Status.CANCELLED || request.isCancelled()) {
            throw new CustomException("Request đã bị hủy, không thể approve", HttpStatus.BAD_REQUEST);
        }

        if (request.getWorkOrder() != null) {
            throw new CustomException("Request này đã có Work Order", HttpStatus.BAD_REQUEST);
        }

        WorkOrder workOrder = new WorkOrder();
        workOrder.setTitle(request.getTitle());
        workOrder.setDescription(request.getDescription());
        workOrder.setDueDate(request.getDueDate());
        workOrder.setStatus(WorkOrder.WorkOrderStatus.OPEN);
        workOrder.setPriority(mapPriority(request.getPriority()));
        workOrder.setArchived(false);

        if (request.getLocation() != null) {
            workOrder.setLocationName(request.getLocation().getName());
        }

        if (request.getAsset() != null) {
            Asset asset = request.getAsset();

            workOrder.setAsset(asset);
            workOrder.setAssetName(asset.getName());

            if (asset.getStatus() != AssetStatus.DECOMMISSIONED) {
                asset.setStatus(AssetStatus.MAINTENANCE);
                assetRepository.save(asset);
            }
        }

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrder);

        request.setWorkOrder(savedWorkOrder);
        request.setStatus(Request.Status.APPROVED);

        Request saved = requestRepository.save(request);

        notifyCurrentUser(
                "Request đã được duyệt",
                "Request \"" + safeTitle(saved.getTitle()) + "\" đã được duyệt và tạo Work Order #" + savedWorkOrder.getId()
        );

        return saved;
    }

    public Request reject(Long id, String reason) {
        Request request = findEntityById(id);

        if (request.getWorkOrder() != null) {
            throw new CustomException("Request đã có Work Order, không thể reject", HttpStatus.BAD_REQUEST);
        }

        request.setStatus(Request.Status.REJECTED);

        if (reason != null && !reason.isBlank()) {
            request.setCancellationReason(reason.trim());
        }

        Request saved = requestRepository.save(request);

        notifyCurrentUser(
                "Request bị từ chối",
                "Request \"" + safeTitle(saved.getTitle()) + "\" đã bị từ chối."
        );

        return saved;
    }

    public Request cancel(Long id, String reason) {
        Request request = findEntityById(id);

        if (request.getStatus() == Request.Status.APPROVED && request.getWorkOrder() != null) {
            throw new CustomException(
                    "Request đã tạo Work Order, không thể cancel trực tiếp",
                    HttpStatus.BAD_REQUEST
            );
        }

        request.setStatus(Request.Status.CANCELLED);
        request.setCancelled(true);
        request.setCancellationReason(
                reason != null && !reason.isBlank() ? reason.trim() : "Request was cancelled"
        );

        Request saved = requestRepository.save(request);

        notifyCurrentUser(
                "Request đã bị hủy",
                "Request \"" + safeTitle(saved.getTitle()) + "\" đã bị hủy."
        );

        return saved;
    }

    public void delete(Long id) {
        Request entity = findEntityById(id);

        if (entity.getWorkOrder() != null) {
            throw new CustomException(
                    "Request đã liên kết Work Order, không thể xóa",
                    HttpStatus.BAD_REQUEST
            );
        }

        requestRepository.delete(entity);

        notifyCurrentUser(
                "Request đã xóa",
                "Request \"" + safeTitle(entity.getTitle()) + "\" đã được xóa."
        );
    }

    private WorkOrder.WorkOrderPriority mapPriority(Request.Priority priority) {
        if (priority == null) {
            return WorkOrder.WorkOrderPriority.NONE;
        }

        return switch (priority) {
            case LOW -> WorkOrder.WorkOrderPriority.LOW;
            case MEDIUM -> WorkOrder.WorkOrderPriority.MEDIUM;
            case HIGH -> WorkOrder.WorkOrderPriority.HIGH;
            case URGENT -> WorkOrder.WorkOrderPriority.URGENT;
            case NONE -> WorkOrder.WorkOrderPriority.NONE;
        };
    }

    private void notifyCurrentUser(String title, String message) {
        try {
            User currentUser = userService.whoami();

            if (currentUser != null && currentUser.getUserId() != null) {
                notificationService.createNotificationIfUserExists(
                        currentUser.getUserId(),
                        title,
                        message
                );
            }
        } catch (Exception ignored) {
        }
    }

    private String safeTitle(String title) {
        return title == null || title.isBlank() ? "Không có tiêu đề" : title.trim();
    }

    public void cancelAndDetachByWorkOrderId(Long workOrderId) {
        if (workOrderId == null) {
            return;
        }

    List<Request> requests = requestRepository.findAll()
            .stream()
            .filter(r -> r.getWorkOrder() != null
                    && r.getWorkOrder().getId() != null
                    && r.getWorkOrder().getId().equals(workOrderId))
            .toList();
            
        for (Request request : requests) {
            request.setWorkOrder(null);
            request.setStatus(Request.Status.CANCELLED);
            request.setCancelled(true);
            request.setCancellationReason("Work Order liên quan đã bị xóa vĩnh viễn");
        }
        requestRepository.saveAll(requests);
    }
    
    public void forceDelete(Long id) {
        Request entity = findEntityById(id);
        
        if (entity.getWorkOrder() != null) {
            entity.setWorkOrder(null);
            entity.setStatus(Request.Status.CANCELLED);
            entity.setCancelled(true);
            entity.setCancellationReason("Request bị xóa vĩnh viễn sau khi ngắt liên kết Work Order");
            requestRepository.save(entity);
        }
        requestRepository.delete(entity);
    }
}