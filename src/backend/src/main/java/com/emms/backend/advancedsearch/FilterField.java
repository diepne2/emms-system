package com.emms.backend.advancedsearch;

import com.emms.backend.entity.enums.EnumName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.criteria.JoinType;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "A single filter condition applied to a search query")
public class FilterField {

    @Schema(description = "Tên field cần filter, ví dụ: status, department.name")
    private String field;

    @Schema(description = "Kiểu join nếu field đi qua relation", example = "INNER")
    private JoinType joinType = JoinType.INNER;

    @Schema(description = "Giá trị đơn của filter")
    private Object value;

    @Schema(description = "Phép toán filter, ví dụ: EQ, LIKE, GT, IN, BETWEEN")
    private String operation;

    @Schema(description = "Danh sách giá trị cho filter nhiều giá trị")
    private List<Object> values = new ArrayList<>();

    @Schema(description = "Các điều kiện thay thế theo kiểu OR")
    private List<FilterField> alternatives = new ArrayList<>();

    @Schema(description = "Tên enum nếu field là enum")
    private EnumName enumName;

    public FilterField() {
    }

    public FilterField(String field, String operation, Object value) {
        this.field = trim(field);
        this.operation = trimUpper(operation);
        this.value = value;
    }

    public FilterField(String field, String operation, List<Object> values) {
        this.field = trim(field);
        this.operation = trimUpper(operation);
        this.values = values != null ? values : new ArrayList<>();
    }

    public boolean isMultiValue() {
        return values != null && !values.isEmpty();
    }

    public boolean hasAlternatives() {
        return alternatives != null && !alternatives.isEmpty();
    }

    public boolean isEnum() {
        return enumName != null;
    }

    public boolean hasSingleValue() {
        return value != null;
    }

    public boolean hasField() {
        return field != null && !field.isBlank();
    }

    public boolean hasOperation() {
        return operation != null && !operation.isBlank();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String trimUpper(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = trim(field);
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType != null ? joinType : JoinType.INNER;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = trimUpper(operation);
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values != null ? values : new ArrayList<>();
    }

    public List<FilterField> getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(List<FilterField> alternatives) {
        this.alternatives = alternatives != null ? alternatives : new ArrayList<>();
    }

    public EnumName getEnumName() {
        return enumName;
    }

    public void setEnumName(EnumName enumName) {
        this.enumName = enumName;
    }
}