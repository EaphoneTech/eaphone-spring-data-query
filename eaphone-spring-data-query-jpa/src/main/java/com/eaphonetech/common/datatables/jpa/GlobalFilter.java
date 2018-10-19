package com.eaphonetech.common.datatables.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringOperation;

/**
 * Filter which creates a basic "WHERE ... LIKE ..." clause
 */
class GlobalFilter implements Filter {
    private final String escapedRawValue;

    GlobalFilter(String filterValue) {
        if (filterValue == null) {
            escapedRawValue = "";
        } else {
            escapedRawValue = escapeValue(filterValue);
        }
    }

    String nullOrTrimmedValue(String value) {
        return "\\NULL".equals(value) ? "NULL" : value.trim();
    }

    private String escapeValue(String filterValue) {
        return "%" + nullOrTrimmedValue(filterValue).toLowerCase().replaceAll("~", "~~").replaceAll("%", "~%")
                .replaceAll("_", "~_") + "%";
    }

    @Override
    public Predicate createPredicate(From<?, ?> from, CriteriaBuilder criteriaBuilder, String attributeName) {
        Expression<?> expression = from.get(attributeName);
        return criteriaBuilder.like(criteriaBuilder.lower(expression.as(String.class)), escapedRawValue, '~');
    }

    @Override
    public com.querydsl.core.types.Predicate createPredicate(PathBuilder<?> pathBuilder, String attributeName) {
        StringOperation path = Expressions.stringOperation(Ops.STRING_CAST, pathBuilder.get(attributeName));
        return path.lower().like(escapedRawValue, '~');
    }
}