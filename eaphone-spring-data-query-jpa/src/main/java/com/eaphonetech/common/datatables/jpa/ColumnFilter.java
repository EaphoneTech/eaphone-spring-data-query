package com.eaphonetech.common.datatables.jpa;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import com.eaphonetech.common.datatables.jpa.columns.AbstractColumnTypeDecorator;
import com.eaphonetech.common.datatables.model.mapping.ColumnType;
import com.eaphonetech.common.datatables.model.mapping.filter.QueryFilter;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;

class ColumnFilter extends GlobalFilter implements Filter {
    private final ColumnType type;
    private QueryFilter filter;

    ColumnFilter(ColumnType type, QueryFilter filter) {
        super(filter.getEq());
        this.type = type;
        this.filter = filter;
    }

    @Override
    public com.querydsl.core.types.Predicate createPredicate(PathBuilder<?> pathBuilder, String attributeName) {
        final PathBuilder<Object> path = pathBuilder.get(attributeName);

        final List<BooleanExpression> ops = new LinkedList<>();

        AbstractColumnTypeDecorator.forType(type).fillOperations(ops, path, filter);

        return Expressions.booleanOperation(Ops.AND, ops.toArray(new BooleanExpression[0]));
    }

    @Override
    public Predicate createPredicate(From<?, ?> from, CriteriaBuilder criteriaBuilder, String attributeName) {
        Expression<?> expression = from.get(attributeName);

        final List<Predicate> predicates = new LinkedList<>();

        AbstractColumnTypeDecorator.forType(type).fillPredicates(predicates, criteriaBuilder, expression, filter);

        /*
         * if (values.isEmpty()) {
         * return addNullCase ? expression.isNull() : criteriaBuilder.conjunction();
         * } else if (isBasicFilter()) {
         * return super.createPredicate(from, criteriaBuilder, attributeName);
         * }
         * 
         * javax.persistence.criteria.Predicate predicate;
         * if (isBooleanComparison) {
         * predicate = expression.in(booleanValues);
         * } else {
         * predicate = expression.as(String.class).in(values);
         * }
         * if (addNullCase)
         * predicate = criteriaBuilder.or(predicate, expression.isNull());
         */

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

}
