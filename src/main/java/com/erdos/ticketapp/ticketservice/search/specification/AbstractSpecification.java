package com.erdos.ticketapp.ticketservice.search.specification;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.Locale;

@Slf4j
@NoArgsConstructor
public abstract class AbstractSpecification<C, T> {

    private static final String QUERY = "query";

    public Specification<@NonNull T> build(C criteria) {
        Specification<@NonNull T> specification = Specification.unrestricted();

        if (criteria == null) {
            return specification;
        }

        for (Field field : FieldUtils.getAllFields(criteria.getClass())) {
            field.setAccessible(true);

            Object value;
            try {
                value = field.get(criteria);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException(
                        "Failed to read criteria field: " + field.getName(),
                        exception);
            }

            if (value == null || value instanceof String string && string.isBlank()) {
                continue;
            }

            String entityPath = QUERY.equals(field.getName()) ? "code" : field.getName();
            specification = specification.and(buildPredicate(entityPath, value));
        }

        return specification;
    }

    protected Specification<@NonNull T> buildPredicate(String entityPath, Object value) {
        return (root, query, criteriaBuilder) -> {
            Path<?> path;

            try {
                path = resolvePath(root, entityPath);
            } catch (IllegalArgumentException exception) {
                log.error("Could not resolve entity path: {}", entityPath, exception);
                return criteriaBuilder.conjunction();
            }

            if (value instanceof String string) {
                return criteriaBuilder.like(
                        criteriaBuilder.lower(path.as(String.class)),
                        "%" + string.toLowerCase(Locale.ROOT) + "%");
            }

            return criteriaBuilder.equal(path, value);
        };
    }

    private Path<?> resolvePath(Root<?> root, String path) {
        Path<?> current = root;
        for (String part : path.split("\\.")) {
            current = current.get(part);
        }
        return current;
    }
}
