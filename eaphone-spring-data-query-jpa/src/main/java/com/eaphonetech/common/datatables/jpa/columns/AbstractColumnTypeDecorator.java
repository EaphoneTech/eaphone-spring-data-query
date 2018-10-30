package com.eaphonetech.common.datatables.jpa.columns;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.springframework.util.StringUtils;

import com.eaphonetech.common.datatables.model.mapping.ColumnType;
import com.eaphonetech.common.datatables.model.mapping.filter.QueryFilter;
import com.eaphonetech.common.datatables.util.DateUtils;
import com.google.common.collect.Lists;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;

public abstract class AbstractColumnTypeDecorator {

	public abstract void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, QueryFilter filter);

	public abstract void fillPredicates(List<Predicate> predicates, CriteriaBuilder crit, Expression<?> expression,
			QueryFilter filter);

	/**
	 * 类似于工厂模式
	 * 
	 * @param type
	 * @return 一定不会为 <code>null</code>
	 */
	public static AbstractColumnTypeDecorator forType(ColumnType type) {
		if (type == null) {
			return EMPTY;
		} else if (ColumnType.STRING.equals(type)) {
			return STRING;
		} else if (ColumnType.INTEGER.equals(type)) {
			return INTEGER;
		} else if (ColumnType.DOUBLE.equals(type)) {
			return DOUBLE;
		} else if (ColumnType.BOOLEAN.equals(type)) {
			return BOOLEAN;
		} else if (ColumnType.DATE.equals(type)) {
			return DATE;
		}
		return EMPTY;
	}

	private static final AbstractColumnTypeDecorator EMPTY = new AbstractColumnTypeDecorator() {
		@Override
		public void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, QueryFilter filter) {
		}

		@Override
		public void fillPredicates(List<Predicate> predicates, CriteriaBuilder crit, Expression<?> expression,
				QueryFilter filter) {
		}
	};

	private static final AbstractColumnTypeDecorator STRING = new AbstractColumnTypeDecorator() {
		@Override
		public void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, QueryFilter filter) {
			if (StringUtils.hasLength(filter.get_eq())) {
				ops.add(Expressions.stringOperation(Ops.STRING_CAST, path).eq(filter.get_eq()));
			}
			if (StringUtils.hasLength(filter.get_ne())) {
				ops.add(Expressions.stringOperation(Ops.STRING_CAST, path).ne(filter.get_ne()));
			}
			if (StringUtils.hasLength(filter.get_in())) {
				String[] parts = filter.get_in().split(",");
				ops.add(Expressions.stringOperation(Ops.IN, path).in(parts));
			}
			if (StringUtils.hasLength(filter.get_nin())) {
				String[] parts = filter.get_nin().split(",");
				ops.add(Expressions.stringOperation(Ops.NOT_IN, path).notIn(parts));
			}
			if (StringUtils.hasLength(filter.get_regex())) {
				ops.add(Expressions.stringOperation(Ops.LIKE, path).like(filter.get_regex()));
			}
			if (StringUtils.hasLength(filter.get_like())) {
				ops.add(Expressions.stringOperation(Ops.LIKE, path).like(filter.get_like()));
			}
			if (filter.get_empty() != null) {
				if (filter.get_empty()) {
					ops.add(Expressions.stringOperation(Ops.STRING_IS_EMPTY, path).isEmpty());
				} else {
					ops.add(Expressions.stringOperation(Ops.STRING_IS_EMPTY, path).isNotEmpty());
				}
			}
			if (filter.get_null() != null) {
				if (filter.get_null()) {
					ops.add(Expressions.stringOperation(Ops.IS_NULL, path).isNull());
				} else {
					ops.add(Expressions.stringOperation(Ops.IS_NOT_NULL, path).isNotNull());
				}
			}
		}

		@Override
		public void fillPredicates(List<Predicate> predicates, CriteriaBuilder crit, Expression<?> expression,
				QueryFilter filter) {
			Expression<String> exp = expression.as(String.class);
			if (StringUtils.hasLength(filter.get_eq())) {
				predicates.add(crit.equal(exp, filter.get_eq()));
			}
			if (StringUtils.hasLength(filter.get_ne())) {
				predicates.add(crit.notEqual(exp, filter.get_ne()));
			}
			if (StringUtils.hasLength(filter.get_in())) {
				List<String> parts = Lists.newArrayList(filter.get_in().split(","));
				predicates.add(exp.in(parts));
			}
			if (StringUtils.hasLength(filter.get_nin())) {
				List<String> parts = Lists.newArrayList(filter.get_nin().split(","));
				predicates.add(crit.not(exp.in(parts)));
			}
			if (StringUtils.hasLength(filter.get_regex())) {
				// TODO Need test here about regular expressions
				// src:
				// https://stackoverflow.com/questions/24995881/use-regular-expressions-in-jpa-criteriabuilder
				predicates.add(//
						crit.equal(//
								crit.function("regexp", Integer.class, exp, crit.literal(filter.get_regex()))//
				, 1));
			}
			if (StringUtils.hasLength(filter.get_like())) {
				// TODO Need test here about whether '%' should be added
				predicates.add(crit.like(exp, filter.get_like()));
			}
			if (filter.get_empty() != null) {
				// TODO Need test here about type cast
				// TODO here isEmpty() is different from previous {@link Ops.STRING_IS_EMPTY}
				Expression<Collection> expc = expression.as(Collection.class);
				if (filter.get_empty()) {
					predicates.add(crit.isEmpty(expc));
				} else {
					predicates.add(crit.isNotEmpty(expc));
				}
			}
			if (filter.get_null() != null) {
				if (filter.get_null()) {
					predicates.add(crit.isNull(exp));
				} else {
					predicates.add(crit.isNotNull(exp));
				}
			}
		}
	};

	private static final AbstractColumnTypeDecorator INTEGER = new AbstractColumnTypeDecorator() {
		private Integer parse(String text) {
			return Integer.parseInt(text);
		}

		private List<Integer> parseList(String text) {
			return Arrays.stream(text.split(",")) //
					.map(String::trim) //
					.filter(StringUtils::hasLength) //
					.map(Integer::parseInt) //
					.collect(Collectors.toList());
		}

		@Override
		public void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, QueryFilter filter) {
			if (StringUtils.hasLength(filter.get_eq())) {
				ops.add(Expressions.numberOperation(Integer.class, Ops.EQ, path).eq(parse(filter.get_eq())));
			}
			if (StringUtils.hasLength(filter.get_ne())) {
				ops.add(Expressions.numberOperation(Integer.class, Ops.NOT, path).ne(parse(filter.get_ne())));
			}
			if (StringUtils.hasLength(filter.get_in())) {
				ops.add(Expressions.numberOperation(Integer.class, Ops.IN, path).in(parseList(filter.get_in())));
			}
			if (StringUtils.hasLength(filter.get_nin())) {
				ops.add(Expressions.numberOperation(Integer.class, Ops.NOT_IN, path)
						.notIn(parseList(filter.get_nin())));
			}
			if (StringUtils.hasLength(filter.get_lt())) {
				ops.add(Expressions.numberOperation(Integer.class, Ops.LT, path).lt(parse(filter.get_lt())));
			}
			if (StringUtils.hasLength(filter.get_lte())) {
				ops.add(Expressions.numberOperation(Integer.class, Ops.LOE, path).loe(parse(filter.get_lte())));
			}
			if (StringUtils.hasLength(filter.get_gt())) {
				ops.add(Expressions.numberOperation(Integer.class, Ops.GT, path).gt(parse(filter.get_gt())));
			}
			if (StringUtils.hasLength(filter.get_gte())) {
				ops.add(Expressions.numberOperation(Integer.class, Ops.GOE, path).goe(parse(filter.get_gte())));
			}
			if (filter.get_null() != null) {
				if (filter.get_null()) {
					ops.add(Expressions.numberOperation(Integer.class, Ops.IS_NULL, path).isNull());
				} else {
					ops.add(Expressions.numberOperation(Integer.class, Ops.IS_NOT_NULL, path).isNotNull());
				}
			}
		}

		@Override
		public void fillPredicates(List<Predicate> predicates, CriteriaBuilder crit, Expression<?> expression,
				QueryFilter filter) {
			Expression<Integer> exp = expression.as(Integer.class);
			if (StringUtils.hasLength(filter.get_eq())) {
				predicates.add(crit.equal(exp, parse(filter.get_eq())));
			}
			if (StringUtils.hasLength(filter.get_ne())) {
				predicates.add(crit.notEqual(exp, parse(filter.get_ne())));
			}
			if (StringUtils.hasLength(filter.get_in())) {
				predicates.add(exp.in(parseList(filter.get_in())));
			}
			if (StringUtils.hasLength(filter.get_nin())) {
				predicates.add(crit.not(exp.in(parseList(filter.get_nin()))));
			}
			if (StringUtils.hasLength(filter.get_lt())) {
				predicates.add(crit.lessThan(exp, parse(filter.get_lt())));
			}
			if (StringUtils.hasLength(filter.get_lte())) {
				predicates.add(crit.le(exp, parse(filter.get_lte())));
			}
			if (StringUtils.hasLength(filter.get_gt())) {
				predicates.add(crit.greaterThan(exp, parse(filter.get_gt())));
			}
			if (StringUtils.hasLength(filter.get_gte())) {
				predicates.add(crit.ge(exp, parse(filter.get_gte())));
			}
			if (filter.get_null() != null) {
				if (filter.get_null()) {
					predicates.add(crit.isNull(exp));
				} else {
					predicates.add(crit.isNotNull(exp));
				}
			}
		}
	};
	private static final AbstractColumnTypeDecorator DOUBLE = new AbstractColumnTypeDecorator() {
		private Double parse(String text) {
			return Double.parseDouble(text);
		}

		private List<Double> parseList(String text) {
			return Arrays.stream(text.split(",")) //
					.map(String::trim) //
					.filter(StringUtils::hasLength) //
					.map(Double::parseDouble) //
					.collect(Collectors.toList());
		}

		@Override
		public void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, QueryFilter filter) {
			if (StringUtils.hasLength(filter.get_eq())) {
				ops.add(Expressions.numberOperation(Double.class, Ops.EQ, path).eq(parse(filter.get_eq())));
			}
			if (StringUtils.hasLength(filter.get_ne())) {
				ops.add(Expressions.numberOperation(Double.class, Ops.NOT, path).ne(parse(filter.get_ne())));
			}
			if (StringUtils.hasLength(filter.get_in())) {
				ops.add(Expressions.numberOperation(Double.class, Ops.IN, path).in(parseList(filter.get_in())));
			}
			if (StringUtils.hasLength(filter.get_nin())) {
				ops.add(Expressions.numberOperation(Double.class, Ops.NOT_IN, path).notIn(parseList(filter.get_nin())));
			}
			if (StringUtils.hasLength(filter.get_lt())) {
				ops.add(Expressions.numberOperation(Double.class, Ops.LT, path).lt(parse(filter.get_lt())));
			}
			if (StringUtils.hasLength(filter.get_lte())) {
				ops.add(Expressions.numberOperation(Double.class, Ops.LOE, path).loe(parse(filter.get_lte())));
			}
			if (StringUtils.hasLength(filter.get_gt())) {
				ops.add(Expressions.numberOperation(Double.class, Ops.GT, path).gt(parse(filter.get_gt())));
			}
			if (StringUtils.hasLength(filter.get_gte())) {
				ops.add(Expressions.numberOperation(Double.class, Ops.GOE, path).goe(parse(filter.get_gte())));
			}
			if (filter.get_null() != null) {
				if (filter.get_null()) {
					ops.add(Expressions.numberOperation(Double.class, Ops.IS_NULL, path).isNull());
				} else {
					ops.add(Expressions.numberOperation(Double.class, Ops.IS_NOT_NULL, path).isNotNull());
				}
			}
		}

		@Override
		public void fillPredicates(List<Predicate> predicates, CriteriaBuilder crit, Expression<?> expression,
				QueryFilter filter) {
			Expression<Double> exp = expression.as(Double.class);
			if (StringUtils.hasLength(filter.get_eq())) {
				predicates.add(crit.equal(exp, parse(filter.get_eq())));
			}
			if (StringUtils.hasLength(filter.get_ne())) {
				predicates.add(crit.notEqual(exp, parse(filter.get_ne())));
			}
			if (StringUtils.hasLength(filter.get_in())) {
				predicates.add(exp.in(parseList(filter.get_in())));
			}
			if (StringUtils.hasLength(filter.get_nin())) {
				predicates.add(crit.not(exp.in(parseList(filter.get_nin()))));
			}
			if (StringUtils.hasLength(filter.get_lt())) {
				predicates.add(crit.lessThan(exp, parse(filter.get_lt())));
			}
			if (StringUtils.hasLength(filter.get_lte())) {
				predicates.add(crit.le(exp, parse(filter.get_lte())));
			}
			if (StringUtils.hasLength(filter.get_gt())) {
				predicates.add(crit.greaterThan(exp, parse(filter.get_gt())));
			}
			if (StringUtils.hasLength(filter.get_gte())) {
				predicates.add(crit.ge(exp, parse(filter.get_gte())));
			}
			if (filter.get_null() != null) {
				if (filter.get_null()) {
					predicates.add(crit.isNull(exp));
				} else {
					predicates.add(crit.isNotNull(exp));
				}
			}
		}
	};
	private static final AbstractColumnTypeDecorator BOOLEAN = new AbstractColumnTypeDecorator() {

		private Boolean parse(String text) {
			return Boolean.parseBoolean(text);
		}

		@Override
		public void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, QueryFilter filter) {
			if (StringUtils.hasLength(filter.get_eq())) {
				ops.add(Expressions.booleanOperation(Ops.EQ, path).eq(parse(filter.get_eq())));
			}
			if (StringUtils.hasLength(filter.get_ne())) {
				ops.add(Expressions.booleanOperation(Ops.NOT, path).ne(parse(filter.get_ne())));
			}
			if (filter.get_null() != null) {
				if (filter.get_null()) {
					ops.add(Expressions.booleanOperation(Ops.IS_NULL, path).isNull());
				} else {
					ops.add(Expressions.booleanOperation(Ops.IS_NOT_NULL, path).isNotNull());
				}
			}
		}

		@Override
		public void fillPredicates(List<Predicate> predicates, CriteriaBuilder crit, Expression<?> expression,
				QueryFilter filter) {
			Expression<Boolean> exp = expression.as(Boolean.class);
			if (StringUtils.hasLength(filter.get_eq())) {
				predicates.add(crit.equal(exp, parse(filter.get_eq())));
			}
			if (StringUtils.hasLength(filter.get_ne())) {
				predicates.add(crit.notEqual(exp, parse(filter.get_ne())));
			}
			if (filter.get_null() != null) {
				if (filter.get_null()) {
					predicates.add(crit.isNull(exp));
				} else {
					predicates.add(crit.isNotNull(exp));
				}
			}
		}
	};
	private static final AbstractColumnTypeDecorator DATE = new AbstractColumnTypeDecorator() {
		private Date parse(String text) {
			return DateUtils.tryParse(DateUtils.FORMAT_YMD, text);
		}

		private List<Date> parseList(String text) {
			return Arrays.stream(text.split(",")) //
					.map(String::trim) //
					.filter(StringUtils::hasLength) //
					.map(p -> DateUtils.tryParse(DateUtils.FORMAT_YMD, p)) //
					.collect(Collectors.toList());
		}

		@Override
		public void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, QueryFilter filter) {
			if (StringUtils.hasLength(filter.get_eq())) {
				ops.add(Expressions.dateOperation(Date.class, Ops.EQ, path).eq(parse(filter.get_eq())));
			}
			if (StringUtils.hasLength(filter.get_ne())) {
				ops.add(Expressions.dateOperation(Date.class, Ops.NOT, path).ne(parse(filter.get_ne())));
			}
			if (StringUtils.hasLength(filter.get_in())) {
				ops.add(Expressions.dateOperation(Date.class, Ops.IN, path).in(parseList(filter.get_in())));
			}
			if (StringUtils.hasLength(filter.get_nin())) {
				ops.add(Expressions.dateOperation(Date.class, Ops.NOT_IN, path).notIn(parseList(filter.get_nin())));
			}
			if (StringUtils.hasLength(filter.get_lt())) {
				ops.add(Expressions.dateOperation(Date.class, Ops.LT, path).lt(parse(filter.get_lt())));
			}
			if (StringUtils.hasLength(filter.get_lte())) {
				ops.add(Expressions.dateOperation(Date.class, Ops.LOE, path).loe(parse(filter.get_lte())));
			}
			if (StringUtils.hasLength(filter.get_gt())) {
				ops.add(Expressions.dateOperation(Date.class, Ops.GT, path).gt(parse(filter.get_gt())));
			}
			if (StringUtils.hasLength(filter.get_gte())) {
				ops.add(Expressions.dateOperation(Date.class, Ops.GOE, path).goe(parse(filter.get_gte())));
			}
			if (filter.get_null() != null) {
				if (filter.get_null().booleanValue()) {
					ops.add(Expressions.dateOperation(Date.class, Ops.IS_NULL, path).isNull());
				} else {
					ops.add(Expressions.dateOperation(Date.class, Ops.IS_NOT_NULL, path).isNotNull());
				}
			}
		}

		@Override
		public void fillPredicates(List<Predicate> predicates, CriteriaBuilder crit, Expression<?> expression,
				QueryFilter filter) {
			Expression<Date> exp = expression.as(Date.class);
			if (StringUtils.hasLength(filter.get_eq())) {
				predicates.add(crit.equal(exp, parse(filter.get_eq())));
			}
			if (StringUtils.hasLength(filter.get_ne())) {
				predicates.add(crit.notEqual(exp, parse(filter.get_ne())));
			}
			if (StringUtils.hasLength(filter.get_in())) {
				predicates.add(exp.in(parseList(filter.get_in())));
			}
			if (StringUtils.hasLength(filter.get_nin())) {
				predicates.add(crit.not(exp.in(parseList(filter.get_nin()))));
			}
			if (StringUtils.hasLength(filter.get_lt())) {
				predicates.add(crit.lessThan(exp, parse(filter.get_lt())));
			}
			if (StringUtils.hasLength(filter.get_lte())) {
				predicates.add(crit.lessThanOrEqualTo(exp, parse(filter.get_lte())));
			}
			if (StringUtils.hasLength(filter.get_gt())) {
				predicates.add(crit.greaterThan(exp, parse(filter.get_gt())));
			}
			if (StringUtils.hasLength(filter.get_gte())) {
				predicates.add(crit.greaterThanOrEqualTo(exp, parse(filter.get_gte())));
			}
			if (filter.get_null() != null) {
				if (filter.get_null()) {
					predicates.add(crit.isNull(exp));
				} else {
					predicates.add(crit.isNotNull(exp));
				}
			}
		}
	};

}
