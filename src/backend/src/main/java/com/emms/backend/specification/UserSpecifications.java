package com.emms.backend.specification;

import com.emms.backend.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> filter(
            String keyword,
            String roleCode,
            Boolean enabled,
            String status
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeValue = "%" + keyword.trim().toLowerCase() + "%";

                Predicate usernameLike = cb.like(cb.lower(root.get("username")), likeValue);
                Predicate emailLike = cb.like(cb.lower(root.get("email")), likeValue);
                Predicate firstNameLike = cb.like(cb.lower(root.get("firstName")), likeValue);
                Predicate lastNameLike = cb.like(cb.lower(root.get("lastName")), likeValue);
                Predicate phoneLike = cb.like(cb.lower(root.get("phone")), likeValue);
                Predicate jobTitleLike = cb.like(cb.lower(root.get("jobTitle")), likeValue);

                predicates.add(cb.or(
                        usernameLike,
                        emailLike,
                        firstNameLike,
                        lastNameLike,
                        phoneLike,
                        jobTitleLike
                ));
            }

            if (roleCode != null && !roleCode.trim().isEmpty()) {
                Join<Object, Object> roleJoin = root.join("role");
                predicates.add(
                        cb.equal(
                                cb.upper(roleJoin.get("code").as(String.class)),
                                normalizeRoleCode(roleCode)
                        )
                );
            }

            if (enabled != null) {
                predicates.add(cb.equal(root.get("enabled"), enabled));
            }

            if (status != null && !status.trim().isEmpty()) {
                predicates.add(
                        cb.equal(
                                cb.upper(root.get("status").as(String.class)),
                                status.trim().toUpperCase()
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String normalizeRoleCode(String roleCode) {
        String value = roleCode.trim().toUpperCase();
        if (value.startsWith("ROLE_")) {
            value = value.substring(5);
        }
        return value;
    }
}