package com.emms.backend.advancedsearch;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class SpecificationBuilder<T> {

    private final List<FilterField> filterFields;
    private final List<Specification<T>> andSpecifications;
    private final List<Specification<T>> orSpecifications;

    public SpecificationBuilder() {
        this.filterFields = new ArrayList<>();
        this.andSpecifications = new ArrayList<>();
        this.orSpecifications = new ArrayList<>();
    }

    public SpecificationBuilder<T> with(FilterField filterField) {
        if (filterField != null && filterField.hasField() && filterField.hasOperation()) {
            this.filterFields.add(filterField);
        }
        return this;
    }

    public SpecificationBuilder<T> with(List<FilterField> filterFields) {
        if (!CollectionUtils.isEmpty(filterFields)) {
            for (FilterField filterField : filterFields) {
                with(filterField);
            }
        }
        return this;
    }

    public SpecificationBuilder<T> and(Specification<T> specification) {
        if (specification != null) {
            this.andSpecifications.add(specification);
        }
        return this;
    }

    public SpecificationBuilder<T> or(Specification<T> specification) {
        if (specification != null) {
            this.orSpecifications.add(specification);
        }
        return this;
    }

    public Specification<T> build() {
        Specification<T> result = Specification.where(null);

        for (FilterField filterField : filterFields) {
            result = result.and(new WrapperSpecification<>(filterField));
        }

        for (Specification<T> specification : andSpecifications) {
            result = result.and(specification);
        }

        for (Specification<T> specification : orSpecifications) {
            result = result.or(specification);
        }

        return result;
    }
}