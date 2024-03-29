package com.eaphonetech.common.datatables.jpa;

import java.util.LinkedList;
import java.util.List;

import com.eaphonetech.common.datatables.jpa.columns.AbstractColumnTypeDecorator;
import com.eaphonetech.common.datatables.model.mapping.filter.QueryFilter;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;

class ColumnFilter implements Filter {
	private QueryFilter filter;

	ColumnFilter(QueryFilter filter) {
		this.filter = filter;
	}

	@Override
	public com.querydsl.core.types.Predicate createPredicate(PathBuilder<?> pathBuilder, String attributeName) {
		final PathBuilder<Object> path = pathBuilder.get(attributeName);

		final List<BooleanExpression> ops = new LinkedList<>();

		AbstractColumnTypeDecorator.forType(pathBuilder.getType(), attributeName).fillOperations(ops, path, filter);

		return Expressions.booleanOperation(Ops.AND, ops.toArray(new BooleanExpression[0]));
	}

	@Override
	public Predicate createPredicate(From<?, ?> from, CriteriaBuilder criteriaBuilder, String attributeName) {
		Expression<?> expression = from.get(attributeName);

		final List<Predicate> predicates = new LinkedList<>();

		AbstractColumnTypeDecorator.forType(from.getJavaType(), attributeName).fillPredicates(predicates,
				criteriaBuilder, expression, filter);

		/*
		 * if (values.isEmpty()) { return addNullCase ? expression.isNull() : criteriaBuilder.conjunction(); } else if
		 * (isBasicFilter()) { return super.createPredicate(from, criteriaBuilder, attributeName); }
		 * 
		 * javax.persistence.criteria.Predicate predicate; if (isBooleanComparison) { predicate =
		 * expression.in(booleanValues); } else { predicate = expression.as(String.class).in(values); } if (addNullCase)
		 * predicate = criteriaBuilder.or(predicate, expression.isNull());
		 */

		return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
	}

}
