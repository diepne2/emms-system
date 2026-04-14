package com.emms.backend.advancedsearch;

import com.emms.backend.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Sort.Direction;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Search criteria for filtering, sorting, and paginating data")
public class SearchCriteria implements Cloneable {

    @Schema(description = "List of filter conditions to apply to the search")
    private List<FilterField> filterFields = new ArrayList<>();

    @Schema(description = "Sort direction for the results", allowableValues = {"ASC", "DESC"})
    private Direction direction = Direction.DESC;

    @Schema(description = "Page number for pagination (0-based)")
    private int pageNum = 0;

    @Schema(description = "Number of results per page")
    private int pageSize = 10;

    @Schema(description = "Field name to sort the results by")
    private String sortField = "createdAt";

    public SearchCriteria() {
    }

    public SearchCriteria(List<FilterField> filterFields,
                          Direction direction,
                          int pageNum,
                          int pageSize,
                          String sortField) {
        this.filterFields = filterFields != null ? filterFields : new ArrayList<>();
        this.direction = direction != null ? direction : Direction.DESC;
        this.pageNum = Math.max(pageNum, 0);
        this.pageSize = pageSize > 0 ? pageSize : 10;
        this.sortField = (sortField == null || sortField.isBlank()) ? "createdAt" : sortField.trim();
    }

    public List<FilterField> getFilterFields() {
        return filterFields;
    }

    public void setFilterFields(List<FilterField> filterFields) {
        this.filterFields = filterFields != null ? filterFields : new ArrayList<>();
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction != null ? direction : Direction.DESC;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = Math.max(pageNum, 0);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize > 0 ? pageSize : 10;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = (sortField == null || sortField.isBlank()) ? "createdAt" : sortField.trim();
    }

    public void addFilter(String field, Object value, String operation) {
        if (field == null || field.isBlank() || operation == null || operation.isBlank()) {
            return;
        }

        if (this.filterFields == null) {
            this.filterFields = new ArrayList<>();
        }

        FilterField filterField = new FilterField();
        filterField.setField(field);
        filterField.setValue(value);
        filterField.setOperation(operation); // FilterField sẽ tự trim + uppercase
        filterField.setValues(new ArrayList<>());

        this.filterFields.add(filterField);
    }

    public void addFilter(String field, Object value, String operation, List<Object> values) {
        if (field == null || field.isBlank() || operation == null || operation.isBlank()) {
            return;
        }

        if (this.filterFields == null) {
            this.filterFields = new ArrayList<>();
        }

        FilterField filterField = new FilterField();
        filterField.setField(field);
        filterField.setValue(value);
        filterField.setOperation(operation); // FilterField sẽ tự trim + uppercase
        filterField.setValues(values != null ? new ArrayList<>(values) : new ArrayList<>());

        this.filterFields.add(filterField);
    }

    public void addFilter(FilterField filterField) {
        if (filterField == null || !filterField.hasField() || !filterField.hasOperation()) {
            return;
        }

        if (this.filterFields == null) {
            this.filterFields = new ArrayList<>();
        }

        this.filterFields.add(copyFilterField(filterField));
    }

    public void filterCreatedBy(User user) {
        if (user == null || user.getUserId() == null) {
            return;
        }

        addFilter("createdBy.id", user.getUserId(), "EQ");
    }

    public void filterAssignedTo(User user) {
        if (user == null || user.getUserId() == null) {
            return;
        }

        addFilter("assignedTo.id", user.getUserId(), "EQ");
    }

    public void filterByDeviceId(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        addFilter("device.id", deviceId, "EQ");
    }

    public void filterByStatus(String status) {
        if (status == null || status.isBlank()) {
            return;
        }

        addFilter("status", status.trim(), "EQ");
    }

    @Override
    public SearchCriteria clone() {
        try {
            SearchCriteria result = (SearchCriteria) super.clone();
            List<FilterField> copiedFilters = new ArrayList<>();

            if (this.filterFields != null) {
                for (FilterField field : this.filterFields) {
                    if (field != null) {
                        copiedFilters.add(copyFilterField(field));
                    }
                }
            }

            result.filterFields = copiedFilters;
            return result;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Cloning SearchCriteria failed", e);
        }
    }

    private FilterField copyFilterField(FilterField source) {
        FilterField target = new FilterField();
        target.setField(source.getField());
        target.setJoinType(source.getJoinType());
        target.setValue(source.getValue());
        target.setOperation(source.getOperation());
        target.setEnumName(source.getEnumName());

        if (source.getValues() != null) {
            target.setValues(new ArrayList<>(source.getValues()));
        } else {
            target.setValues(new ArrayList<>());
        }

        List<FilterField> copiedAlternatives = new ArrayList<>();
        if (source.getAlternatives() != null) {
            for (FilterField alternative : source.getAlternatives()) {
                if (alternative != null) {
                    copiedAlternatives.add(copyFilterField(alternative));
                }
            }
        }
        target.setAlternatives(copiedAlternatives);

        return target;
    }
}