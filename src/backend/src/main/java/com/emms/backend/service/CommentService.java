package com.emms.backend.service;

import com.emms.backend.dto.comment.CommentCriteria;
import com.emms.backend.dto.comment.CommentPatchDTO;
import com.emms.backend.dto.comment.CommentPostDTO;
import com.emms.backend.entity.Comment;
import com.emms.backend.entity.Notification;
import com.emms.backend.entity.User;
import com.emms.backend.entity.WorkOrder;
import com.emms.backend.entity.enums.NotificationType;
import com.emms.backend.exception.CustomException;
import com.emms.backend.infrastructure.MailServiceFactory;
import com.emms.backend.service.NotificationService;
import com.emms.backend.mapper.CommentMapper;
import com.emms.backend.repository.CommentRepository;
import com.emms.backend.repository.UserRepository;
import com.emms.backend.utils.Helper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final WorkOrderService workOrderService;
    private final EntityManager em;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MessageSource messageSource;
    private final MailServiceFactory mailServiceFactory;

    @Value("${frontend.url}")
    private String frontendUrl;

    public Comment create(@Valid CommentPostDTO commentReq, User user) {
        if (commentReq == null) {
            throw new CustomException("Comment data must not be null", HttpStatus.BAD_REQUEST);
        }
        if (user == null) {
            throw new CustomException("Current user must not be null", HttpStatus.UNAUTHORIZED);
        }

        WorkOrder workOrder = workOrderService.checkAccessToWorkOrderId(commentReq.getWorkOrderId(), user);

        Comment comment = commentMapper.fromPostDto(commentReq);
        comment.setWorkOrder(workOrder);
        comment.setUser(user);

        Comment savedComment = commentRepository.saveAndFlush(comment);
        em.refresh(savedComment);

        Set<User> notifiedUsers = getNotifiedUsers(savedComment, user);
        sendCommentNotifications(savedComment, workOrder, notifiedUsers, user, false);

        return savedComment;
    }

    @Transactional(readOnly = true)
    public List<Comment> getAll() {
        return commentRepository.findAll();
    }

    public void delete(Long id) {
        Comment savedComment = findEntityById(id);
        commentRepository.delete(savedComment);
    }

    @Transactional(readOnly = true)
    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Comment findEntityById(Long id) {
        if (id == null) {
            throw new CustomException("Comment id must not be null", HttpStatus.BAD_REQUEST);
        }

        return commentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Comment not found", HttpStatus.NOT_FOUND));
    }

    public Comment update(Long id, CommentPatchDTO commentPatchDTO, User user) {
        if (id == null) {
            throw new CustomException("Comment id must not be null", HttpStatus.BAD_REQUEST);
        }
        if (commentPatchDTO == null) {
            throw new CustomException("Comment update data must not be null", HttpStatus.BAD_REQUEST);
        }
        if (user == null) {
            throw new CustomException("Current user must not be null", HttpStatus.UNAUTHORIZED);
        }

        Comment savedComment = findEntityById(id);

        if (savedComment.getUser() == null
                || !Objects.equals(extractUserId(savedComment.getUser()), extractUserId(user))) {
            throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        }

        WorkOrder workOrder = workOrderService.checkAccessToWorkOrderId(
                extractWorkOrderId(savedComment.getWorkOrder()),
                user
        );

        commentMapper.updateComment(savedComment, commentPatchDTO);

        Comment updatedComment = commentRepository.saveAndFlush(savedComment);
        em.refresh(updatedComment);

        Set<User> notifiedUsers = getNotifiedUsers(updatedComment, user);
        sendCommentNotifications(updatedComment, workOrder, notifiedUsers, user, true);

        return updatedComment;
    }

    @Transactional(readOnly = true)
    public List<Comment> findByCriteria(CommentCriteria criteria, User user) {
        if (criteria == null || criteria.getWorkOrderId() == null) {
            throw new CustomException("Work order id must not be null", HttpStatus.BAD_REQUEST);
        }
        if (user == null) {
            throw new CustomException("Current user must not be null", HttpStatus.UNAUTHORIZED);
        }

        workOrderService.checkAccessToWorkOrderId(criteria.getWorkOrderId(), user);

        Specification<Comment> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("workOrder").get("workOrderId"), criteria.getWorkOrderId()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return commentRepository.findAll(specification);
    }

    @Transactional(readOnly = true)
    public long countByWorkOrderId(Long workOrderId, User user) {
        if (workOrderId == null) {
            throw new CustomException("Work order id must not be null", HttpStatus.BAD_REQUEST);
        }
        if (user == null) {
            throw new CustomException("Current user must not be null", HttpStatus.UNAUTHORIZED);
        }

        workOrderService.checkAccessToWorkOrderId(workOrderId, user);
        return commentRepository.countByWorkOrderId(workOrderId);
    }

    private Set<User> getNotifiedUsers(Comment comment, User actor) {
        Set<Long> taggedUserIds = comment.extractTaggedUserIds();

        if (taggedUserIds == null || taggedUserIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        return userRepository.findAllById(taggedUserIds)
                .stream()
                .filter(Objects::nonNull)
                .filter(u -> !Objects.equals(extractUserId(u), extractUserId(actor)))
                .sorted(Comparator.comparing(this::extractUserId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String formatCommentContent(String content) {
        if (content == null) {
            return "";
        }

        return content.replaceAll(
                "@\\[(.*?)\\]\\(user:(\\d+)\\)",
                "<a href=\"" + frontendUrl + "/app/people-teams/people/$2\">@$1</a>"
        );
    }

    @Async
    public void sendCommentNotifications(Comment comment,
                                         WorkOrder workOrder,
                                         Set<User> notifiedUsers,
                                         User actor,
                                         boolean isUpdate) {
        if (comment == null || workOrder == null || actor == null) {
            return;
        }
        if (notifiedUsers == null || notifiedUsers.isEmpty()) {
            return;
        }

        Locale locale = Helper.getLocale(actor);
        String notificationKey = isUpdate ? "notification_comment_updated" : "notification_new_comment";
        String emailTitleKey = isUpdate ? "comment_updated" : "new_comment";
        String emailTemplate = isUpdate ? "comment-updated.html" : "new-comment.html";

        String message = messageSource.getMessage(
                notificationKey,
                new Object[]{actor.getFullName(), workOrder.getTitle()},
                locale
        );

        Long workOrderId = extractWorkOrderId(workOrder);

        List<Notification> notifications = notifiedUsers.stream()
                .map(notifiedUser -> new Notification(
                        message,
                        notifiedUser,
                        NotificationType.WORK_ORDER,
                        workOrderId
                ))
                .toList();

        notificationService.createMultiple(
                notifications,
                true,
                messageSource.getMessage(emailTitleKey, null, locale)
        );

        sendCommentEmail(comment, workOrder, notifiedUsers, actor, locale, emailTitleKey, emailTemplate);
    }

    private void sendCommentEmail(Comment comment,
                                  WorkOrder workOrder,
                                  Set<User> notifiedUsers,
                                  User actor,
                                  Locale locale,
                                  String emailTitleKey,
                                  String emailTemplate) {
        String commentContent = formatCommentContent(comment.getContent());
        String commentLink = frontendUrl
                + "/app/work-orders/"
                + extractWorkOrderId(workOrder)
                + "?commentId="
                + comment.getId();

        Map<String, Object> mailVariables = new HashMap<>();
        mailVariables.put("userFullName", actor.getFullName());
        mailVariables.put("workOrderTitle", workOrder.getTitle());
        mailVariables.put("commentContent", commentContent);
        mailVariables.put("commentLink", commentLink);

        Collection<User> usersToMail = notifiedUsers.stream()
                .filter(Objects::nonNull)
                .filter(User::isEnabled)
                .filter(u -> u.getEmail() != null && !u.getEmail().isBlank())
                .collect(Collectors.toList());

        if (!usersToMail.isEmpty()) {
            mailServiceFactory.getMailService().sendMessageUsingThymeleafTemplate(
                    usersToMail.stream().map(User::getEmail).toArray(String[]::new),
                    messageSource.getMessage(emailTitleKey, null, locale),
                    mailVariables,
                    emailTemplate,
                    Helper.getLocale(usersToMail.iterator().next())
            );
        }
    }

    private Long extractUserId(User user) {
        return user == null ? null : user.getUserId();
    }

    private Long extractWorkOrderId(WorkOrder workOrder) {
        return workOrder == null ? null : workOrder.getId();
    }
}