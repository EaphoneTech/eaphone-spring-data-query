package com.eaphonetech.common.datatables.jpa.columns;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.springframework.util.StringUtils;

import com.eaphonetech.common.datatables.model.mapping.ColumnType;
import com.eaphonetech.common.datatables.model.mapping.Filter;
import com.google.common.collect.Lists;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractColumnTypeDecorator {

    public abstract void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, Filter filter);

    public abstract void fillPredicates(List<Predicate> predicates, CriteriaBuilder crit, Expression<?> expression,
            Filter filter);

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
        public void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, Filter filter) {
        }

        @Override
        public void fillPredicates(List<Predicate> predicates, CriteriaBuilder crit, Expression<?> expression,
                Filter filter) {
        }
    };

    private static final AbstractColumnTypeDecorator STRING = new AbstractColumnTypeDecorator() {
        @Override
        public void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, Filter filter) {
            if (StringUtils.hasLength(filter.getEq())) {
                ops.add(Expressions.stringOperation(Ops.STRING_CAST, path).eq(filter.getEq()));
            }
            if (StringUtils.hasLength(filter.getNe())) {
                ops.add(Expressions.stringOperation(Ops.STRING_CAST, path).eq(filter.getNe()).not());
            }
            if (StringUtils.hasLength(filter.getIn())) {
                String[] parts = filter.getIn().split(",");
                ops.add(Expressions.stringOperation(Ops.IN, path).in(parts));
            }
            if (StringUtils.hasLength(filter.getNin())) {
                String[] parts = filter.getNin().split(",");
                ops.add(Expressions.stringOperation(Ops.NOT_IN, path).notIn(parts));
            }
            if (StringUtils.hasLength(filter.getRegex())) {
                ops.add(Expressions.stringOperation(Ops.LIKE, path).like(filter.getRegex()));
            }
            if (filter.getIsEmpty() != null) {
                if (filter.getIsEmpty()) {
                    ops.add(Expressions.stringOperation(Ops.STRING_IS_EMPTY, path).isEmpty());
                } else {
                    ops.add(Expressions.stringOperation(Ops.STRING_IS_EMPTY, path).isNotEmpty());
                }
            }
            if (filter.getIsNull() != null) {
                if (filter.getIsNull()) {
                    ops.add(Expressions.stringOperation(Ops.IS_NULL, path).isNull());
                } else {
                    ops.add(Expressions.stringOperation(Ops.IS_NULL, path).isNull().not());
                }
            }
        }

        @Override
        public void fillPredicates(List<Predicate> predicates, CriteriaBuilder crit, Expression<?> expression,
                Filter filter) {
            Expression<String> exp = expression.as(String.class);
            if (StringUtils.hasLength(filter.getEq())) {
                predicates.add(crit.equal(exp, filter.getEq()));
            }
            if (StringUtils.hasLength(filter.getNe())) {
                predicates.add(crit.notEqual(exp, filter.getNe()));
            }
            if (StringUtils.hasLength(filter.getIn())) {
                List<String> parts = Lists.newArrayList(filter.getIn().split(","));
                predicates.add(exp.in(parts));
            }
            if (StringUtils.hasLength(filter.getNin())) {
                List<String> parts = Lists.newArrayList(filter.getNin().split(","));
                predicates.add(crit.not(exp.in(parts)));
            }
            if (StringUtils.hasLength(filter.getRegex())) {
                predicates.add(crit.like(exp, filter.getRegex()));
            }
            if (filter.getIsEmpty() != null) {
                // TODO 完成这个逻辑
                log.warn("isEmpty() 尚未实现");
                if (filter.getIsEmpty()) {
                } else {
                }
            }
            if (filter.getIsNull() != null) {
                if (filter.getIsNull()) {
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

        @Override
        public void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, Filter filter) {
            if (StringUtils.hasLength(filter.getEq())) {
                ops.add(Expressions.numberOperation(Integer.class, Ops.EQ, path).eq(parse(filter.getEq())));
            }
            if (StringUtils.hasLength(filter.getNe())) {
                ops.add(Expressions.numberOperation(Integer.class, Ops.NOT, path).eq(parse(filter.getNe())));
            }
            if (StringUtils.hasLength(filter.getIn())) {
                List<Integer> parts = Arrays.stream(filter.getIn().split(",")).map(String::trim).map(Integer::parseInt)
                        .collect(Collectors.toList());
                ops.add(Expressions.numberOperation(Integer.class, Ops.IN, path).in(parts));
            }
            if (StringUtils.hasLength(filter.getNin())) {
                List<Integer> parts = Arrays.stream(filter.getIn().split(",")).map(String::trim).map(Integer::parseInt)
                        .collect(Collectors.toList());
                ops.add(Expressions.numberOperation(Integer.class, Ops.NOT_IN, path).notIn(parts));
            }
            if (StringUtils.hasLength(filter.getLt())) {
                ops.add(Expressions.numberOperation(Integer.class, Ops.LT, path).lt(parse(filter.getLt())));
            }
            if (StringUtils.hasLength(filter.getLte())) {
                ops.add(Expressions.numberOperation(Integer.class, Ops.LOE, path).loe(parse(filter.getLte())));
            }
            if (StringUtils.hasLength(filter.getGt())) {
                ops.add(Expressions.numberOperation(Integer.class, Ops.GT, path).lt(parse(filter.getGt())));
            }
            if (StringUtils.hasLength(filter.getGte())) {
                ops.add(Expressions.numberOperation(Integer.class, Ops.GOE, path).loe(parse(filter.getGte())));
            }
            if (filter.getIsNull() != null) {
                if (filter.getIsNull()) {
                    ops.add(Expressions.numberOperation(Integer.class, Ops.IS_NULL, path).isNull());
                } else {
                    ops.add(Expressions.numberOperation(Integer.class, Ops.IS_NULL, path).isNull().not());
                }
            }
        }

        @Override
        public void fillPredicates(List<Predicate> predicates, CriteriaBuilder crit, Expression<?> expression,
                Filter filter) {
            Expression<Integer> exp = expression.as(Integer.class);
            if (StringUtils.hasLength(filter.getEq())) {
                predicates.add(crit.equal(exp, parse(filter.getEq())));
            }
            if (StringUtils.hasLength(filter.getNe())) {
                predicates.add(crit.notEqual(exp, parse(filter.getNe())));
            }
            if (StringUtils.hasLength(filter.getIn())) {
                List<Integer> parts = Arrays.stream(filter.getIn().split(",")).map(String::trim).map(Integer::parseInt)
                        .collect(Collectors.toList());
                predicates.add(exp.in(parts));
            }
            if (StringUtils.hasLength(filter.getNin())) {
                List<Integer> parts = Arrays.stream(filter.getIn().split(",")).map(String::trim).map(Integer::parseInt)
                        .collect(Collectors.toList());
                predicates.add(crit.not(exp.in(parts)));
            }
            if (StringUtils.hasLength(filter.getLt())) {
                predicates.add(crit.lessThan(exp, parse(filter.getLt())));
            }
            if (StringUtils.hasLength(filter.getLte())) {
                predicates.add(crit.le(exp, parse(filter.getLte())));
            }
            if (StringUtils.hasLength(filter.getGt())) {
                predicates.add(crit.greaterThan(exp, parse(filter.getGt())));
            }
            if (StringUtils.hasLength(filter.getGte())) {
                predicates.add(crit.ge(exp, parse(filter.getGte())));
            }
            if (filter.getIsNull() != null) {
                if (filter.getIsNull()) {
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

        @Override
        public void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, Filter filter) {
            if (StringUtils.hasLength(filter.getEq())) {
                ops.add(Expressions.numberOperation(Double.class, Ops.EQ, path).eq(parse(filter.getEq())));
            }
            if (StringUtils.hasLength(filter.getNe())) {
                ops.add(Expressions.numberOperation(Double.class, Ops.NOT, path).eq(parse(filter.getNe())));
            }
            if (StringUtils.hasLength(filter.getIn())) {
                List<Double> parts = Arrays.stream(filter.getIn().split(",")).map(String::trim).map(Double::parseDouble)
                        .collect(Collectors.toList());
                ops.add(Expressions.numberOperation(Double.class, Ops.IN, path).in(parts));
            }
            if (StringUtils.hasLength(filter.getNin())) {
                List<Double> parts = Arrays.stream(filter.getIn().split(",")).map(String::trim).map(Double::parseDouble)
                        .collect(Collectors.toList());
                ops.add(Expressions.numberOperation(Double.class, Ops.NOT_IN, path).notIn(parts));
            }
            if (StringUtils.hasLength(filter.getLt())) {
                ops.add(Expressions.numberOperation(Double.class, Ops.LT, path).lt(parse(filter.getLt())));
            }
            if (StringUtils.hasLength(filter.getLte())) {
                ops.add(Expressions.numberOperation(Double.class, Ops.LOE, path).loe(parse(filter.getLte())));
            }
            if (StringUtils.hasLength(filter.getGt())) {
                ops.add(Expressions.numberOperation(Double.class, Ops.GT, path).lt(parse(filter.getGt())));
            }
            if (StringUtils.hasLength(filter.getGte())) {
                ops.add(Expressions.numberOperation(Double.class, Ops.GOE, path).loe(parse(filter.getGte())));
            }
            if (filter.getIsNull() != null) {
                if (filter.getIsNull()) {
                    ops.add(Expressions.numberOperation(Double.class, Ops.IS_NULL, path).isNull());
                } else {
                    ops.add(Expressions.numberOperation(Double.class, Ops.IS_NULL, path).isNull().not());
                }
            }
        }

        @Override
        public void fillPredicates(List<Predicate> predicates, CriteriaBuilder crit, Expression<?> expression,
                Filter filter) {
            Expression<Double> exp = expression.as(Double.class);
            if (StringUtils.hasLength(filter.getEq())) {
                predicates.add(crit.equal(exp, parse(filter.getEq())));
            }
            if (StringUtils.hasLength(filter.getNe())) {
                predicates.add(crit.notEqual(exp, parse(filter.getNe())));
            }
            if (StringUtils.hasLength(filter.getIn())) {
                List<Integer> parts = Arrays.stream(filter.getIn().split(",")).map(String::trim).map(Integer::parseInt)
                        .collect(Collectors.toList());
                predicates.add(exp.in(parts));
            }
            if (StringUtils.hasLength(filter.getNin())) {
                List<Integer> parts = Arrays.stream(filter.getIn().split(",")).map(String::trim).map(Integer::parseInt)
                        .collect(Collectors.toList());
                predicates.add(crit.not(exp.in(parts)));
            }
            if (StringUtils.hasLength(filter.getLt())) {
                predicates.add(crit.lessThan(exp, parse(filter.getLt())));
            }
            if (StringUtils.hasLength(filter.getLte())) {
                predicates.add(crit.le(exp, parse(filter.getLte())));
            }
            if (StringUtils.hasLength(filter.getGt())) {
                predicates.add(crit.greaterThan(exp, parse(filter.getGt())));
            }
            if (StringUtils.hasLength(filter.getGte())) {
                predicates.add(crit.ge(exp, parse(filter.getGte())));
            }
            if (filter.getIsNull() != null) {
                if (filter.getIsNull()) {
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
        public void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, Filter filter) {
            if (StringUtils.hasLength(filter.getEq())) {
                ops.add(Expressions.booleanOperation(Ops.EQ, path).eq(parse(filter.getEq())));
            }
            if (StringUtils.hasLength(filter.getNe())) {
                ops.add(Expressions.booleanOperation(Ops.NOT, path).eq(parse(filter.getNe())));
            }
            if (filter.getIsNull() != null) {
                if (filter.getIsNull()) {
                    ops.add(Expressions.booleanOperation(Ops.IS_NULL, path).isNull());
                } else {
                    ops.add(Expressions.booleanOperation(Ops.IS_NULL, path).isNull().not());
                }
            }
        }

        @Override
        public void fillPredicates(List<Predicate> predicates, CriteriaBuilder crit, Expression<?> expression,
                Filter filter) {
            Expression<Boolean> exp = expression.as(Boolean.class);
            if (StringUtils.hasLength(filter.getEq())) {
                predicates.add(crit.equal(exp, parse(filter.getEq())));
            }
            if (StringUtils.hasLength(filter.getNe())) {
                predicates.add(crit.notEqual(exp, parse(filter.getNe())));
            }
            if (filter.getIsNull() != null) {
                if (filter.getIsNull()) {
                    predicates.add(crit.isNull(exp));
                } else {
                    predicates.add(crit.isNotNull(exp));
                }
            }
        }
    };
    private static final AbstractColumnTypeDecorator DATE = new AbstractColumnTypeDecorator() {
        private Date parse(String text) {
            // TODO
            log.warn("尚未实现");
            return null;
        }

        @Override
        public void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, Filter filter) {
            if (StringUtils.hasLength(filter.getEq())) {
                ops.add(Expressions.dateOperation(Date.class, Ops.EQ, path).eq(parse(filter.getEq())));
            }
            if (StringUtils.hasLength(filter.getNe())) {
                ops.add(Expressions.dateOperation(Date.class, Ops.NOT, path).eq(parse(filter.getNe())));
            }
            if (StringUtils.hasLength(filter.getIn())) {
                List<Date> parts = Arrays.stream(filter.getIn().split(",")).map(String::trim).map(r -> new Date())
                        .collect(Collectors.toList());
                ops.add(Expressions.dateOperation(Date.class, Ops.IN, path).in(parts));
            }
            if (StringUtils.hasLength(filter.getNin())) {
                List<Date> parts = Arrays.stream(filter.getIn().split(",")).map(String::trim).map(r -> new Date())
                        .collect(Collectors.toList());
                ops.add(Expressions.dateOperation(Date.class, Ops.NOT_IN, path).notIn(parts));
            }
            if (StringUtils.hasLength(filter.getLt())) {
                ops.add(Expressions.dateOperation(Date.class, Ops.LT, path).lt(parse(filter.getLt())));
            }
            if (StringUtils.hasLength(filter.getLte())) {
                ops.add(Expressions.dateOperation(Date.class, Ops.LOE, path).loe(parse(filter.getLte())));
            }
            if (StringUtils.hasLength(filter.getGt())) {
                ops.add(Expressions.dateOperation(Date.class, Ops.GT, path).lt(parse(filter.getGt())));
            }
            if (StringUtils.hasLength(filter.getGte())) {
                ops.add(Expressions.dateOperation(Date.class, Ops.GOE, path).loe(parse(filter.getGte())));
            }
            if (filter.getIsNull() != null) {
                if (filter.getIsNull()) {
                    ops.add(Expressions.dateOperation(Date.class, Ops.IS_NULL, path).isNull());
                } else {
                    ops.add(Expressions.dateOperation(Date.class, Ops.IS_NULL, path).isNull().not());
                }
            }
        }

        @Override
        public void fillPredicates(List<Predicate> predicates, CriteriaBuilder crit, Expression<?> expression,
                Filter filter) {
            Expression<Date> exp = expression.as(Date.class);
            if (StringUtils.hasLength(filter.getEq())) {
                predicates.add(crit.equal(exp, parse(filter.getEq())));
            }
            if (StringUtils.hasLength(filter.getNe())) {
                predicates.add(crit.notEqual(exp, parse(filter.getNe())));
            }
            if (StringUtils.hasLength(filter.getIn())) {
                List<Integer> parts = Arrays.stream(filter.getIn().split(",")).map(String::trim).map(Integer::parseInt)
                        .collect(Collectors.toList());
                predicates.add(exp.in(parts));
            }
            if (StringUtils.hasLength(filter.getNin())) {
                List<Integer> parts = Arrays.stream(filter.getIn().split(",")).map(String::trim).map(Integer::parseInt)
                        .collect(Collectors.toList());
                predicates.add(crit.not(exp.in(parts)));
            }
            if (StringUtils.hasLength(filter.getLt())) {
                predicates.add(crit.lessThan(exp, parse(filter.getLt())));
            }
            if (StringUtils.hasLength(filter.getLte())) {
                predicates.add(crit.greaterThanOrEqualTo(exp, parse(filter.getLte())));
            }
            if (StringUtils.hasLength(filter.getGt())) {
                predicates.add(crit.greaterThan(exp, parse(filter.getGt())));
            }
            if (StringUtils.hasLength(filter.getGte())) {
                predicates.add(crit.greaterThanOrEqualTo(exp, parse(filter.getGte())));
            }
            if (filter.getIsNull() != null) {
                if (filter.getIsNull()) {
                    predicates.add(crit.isNull(exp));
                } else {
                    predicates.add(crit.isNotNull(exp));
                }
            }
        }
    };

}
