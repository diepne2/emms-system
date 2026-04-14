package com.emms.backend.advancedsearch;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

@SuppressWarnings({"rawtypes", "unchecked"})
public class WrapperSpecification<T> implements Specification<T> {

    private final FilterField filterField;

    public WrapperSpecification(FilterField filterField) {
        this.filterField = filterField;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        if (filterField == null || !filterField.hasField() || !filterField.hasOperation()) {
            return cb.conjunction();
        }

        SearchOperation operation = SearchOperation.fromValue(filterField.getOperation());
        if (operation == null) {
            return cb.conjunction();
        }

        Path path = getPath(root, filterField.getField());
        Object value = filterField.getValue();

        switch (operation) {
            case EQUAL:
                return cb.equal(path, value);

            case NOT_EQUAL:
                return cb.notEqual(path, value);

            case CONTAINS:
                return cb.like(cb.lower(path.as(String.class)),
                        "%" + safeString(value).toLowerCase() + "%");

            case DOES_NOT_CONTAIN:
                return cb.notLike(cb.lower(path.as(String.class)),
                        "%" + safeString(value).toLowerCase() + "%");

            case BEGINS_WITH:
                return cb.like(cb.lower(path.as(String.class)),
                        safeString(value).toLowerCase() + "%");

            case DOES_NOT_BEGIN_WITH:
                return cb.notLike(cb.lower(path.as(String.class)),
                        safeString(value).toLowerCase() + "%");

            case ENDS_WITH:
                return cb.like(cb.lower(path.as(String.class)),
                        "%" + safeString(value).toLowerCase());

            case DOES_NOT_END_WITH:
                return cb.notLike(cb.lower(path.as(String.class)),
                        "%" + safeString(value).toLowerCase());

            case NUL:
                return cb.isNull(path);

            case NOT_NULL:
                return cb.isNotNull(path);

            case GREATER_THAN:
                return cb.greaterThan(path, (Comparable) value);

            case GREATER_THAN_EQUAL:
                return cb.greaterThanOrEqualTo(path, (Comparable) value);

            case LESS_THAN:
                return cb.lessThan(path, (Comparable) value);

            case LESS_THAN_EQUAL:
                return cb.lessThanOrEqualTo(path, (Comparable) value);

            case IN:
                CriteriaBuilder.In<Object> inClause = cb.in(path);
                if (filterField.getValues() != null) {
                    for (Object item : filterField.getValues()) {
                        inClause.value(item);
                    }
                }
                return inClause;

            default:
                return cb.conjunction();
        }
    }

    private Path getPath(Path root, String field) {
        String[] parts = field.split("\\.");
        Path path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

    private String safeString(Object value) {
        return value == null ? "" : value.toString();
    }
}