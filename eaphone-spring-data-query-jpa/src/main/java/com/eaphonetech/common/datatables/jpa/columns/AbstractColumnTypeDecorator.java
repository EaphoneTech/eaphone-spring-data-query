package com.eaphonetech.common.datatables.jpa.columns;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.eaphonetech.common.datatables.model.mapping.filter.QueryFilter;
import com.eaphonetech.common.datatables.util.DateUtils;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

public abstract class AbstractColumnTypeDecorator {

    public abstract void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, QueryFilter filter);

    public abstract void fillPredicates(List<Predicate> predicates, CriteriaBuilder crit, Expression<?> expression,
            QueryFilter filter);

    /**
     * 根据类和属性名，转换成AbstractColumnTypeDecorator
     *
     * @param clazz 类
     * @param attributeName 属性名
     * @return 一定不会为 <code>null</code>
     */
    public static AbstractColumnTypeDecorator forType(Class<?> clazz, String attributeName) {
        if (clazz == null || attributeName == null) {
            return EMPTY;
        }
        // TODO 嵌套关联的支持
        Field field = ReflectionUtils.findField(clazz, attributeName);

        final Class<?> fieldType = field.getType();
        if (String.class.isAssignableFrom(fieldType) || Character.class.isAssignableFrom(fieldType)
                || Character.TYPE.equals(fieldType)) {
            return STRING;
        } else if (Byte.TYPE.equals(fieldType)
                || Short.TYPE.equals(fieldType) || Integer.TYPE.equals(fieldType) || Long.TYPE.equals(fieldType)) {
            return INTEGER;
        } else if (Number.class.isAssignableFrom(fieldType) || Float.TYPE.equals(fieldType) || Double.TYPE.equals(fieldType)) {
            return DOUBLE;
        } else if (Boolean.TYPE.equals(fieldType) || Boolean.class.isAssignableFrom(fieldType)) {
            return BOOLEAN;
        } else if (Date.class.isAssignableFrom(fieldType)) {
            return DATE;
        }
        return STRING;
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

    private static final <U extends Object> List<U> convert(List<Object> list,
            Function<? super Object, ? extends U> converter) {
        return list.stream().filter(item -> item != null).map(converter).collect(Collectors.toList());
    }

    private static final BooleanExpression or(PathBuilder<Object> path, @Nonnull BooleanExpression exp1,
            @Nullable BooleanExpression exp2) {
        if (exp2 == null) {
            return exp1;
        }
        return Expressions.booleanOperation(Ops.OR, exp1, exp2);
    }

    private static final Predicate or(CriteriaBuilder crit, @Nonnull Predicate pred1,
            @Nullable Predicate pred2) {
        if (pred2 == null) {
            return pred1;
        }
        return crit.or(pred1, pred2);
    }

    private static final AbstractColumnTypeDecorator STRING = new AbstractColumnTypeDecorator() {
        private String parse(Object o) {
            return o == null ? null : o.toString();
        }

        @Override
        public void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, QueryFilter filter) {
            if (filter.get_eq() != null) {
                ops.add(Expressions.stringOperation(Ops.STRING_CAST, path).eq(parse(filter.get_eq())));
            }
            if (filter.get_ne() != null) {
                ops.add(Expressions.stringOperation(Ops.STRING_CAST, path).ne(parse(filter.get_ne())));
            }
            if (filter.get_isvoid() != null) {
                if (filter.get_isvoid().booleanValue()) {
                    // exp IS NULL OR exp = ''
                    ops.add(Expressions.booleanOperation(Ops.OR, Expressions.stringOperation(Ops.IS_NULL, path),
                            Expressions.stringOperation(Ops.EQ, path).eq("")));
                } else {
                    // exp IS NOT NULL AND exp != ''
                    ops.add(Expressions.stringOperation(Ops.IS_NOT_NULL, path).ne(""));
                }
            }
            if (filter.get_in() != null) {
                List<String> parts = convert(filter.get_in(), Object::toString);
                BooleanExpression exp = null;
                if (!parts.isEmpty()) {
                    exp = Expressions.stringOperation(Ops.IN, path).in(parts);
                }

                if (filter.get_in().contains(null)) {
                    exp = or(path, path.isNull(), exp);
                }
                if (exp != null) {
                    ops.add(exp);
                }
            }
            if (filter.get_nin() != null) {
                List<String> parts = convert(filter.get_nin(), Object::toString);
                BooleanExpression exp = null;
                if (!parts.isEmpty()) {
                    exp = Expressions.stringOperation(Ops.NOT_IN, path).notIn(parts);
                }

                if (filter.get_nin().contains(null)) {
                    exp = or(path, path.isNotNull(), exp);
                }
                if (exp != null) {
                    ops.add(exp);
                }
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
            if (filter.get_eq() != null) {
                predicates.add(crit.equal(exp, filter.get_eq()));
            }
            if (filter.get_ne() != null) {
                predicates.add(crit.notEqual(exp, filter.get_ne()));
            }
            if (filter.get_isvoid() != null) {
                if (filter.get_isvoid().booleanValue()) {
                    // exp IS NULL OR exp = ''
                    predicates.add(crit.or(exp.isNull(), crit.equal(exp, "")));
                } else {
                    // exp IS NOT NULL AND exp != ''
                    predicates.add(crit.and(exp.isNotNull(), crit.notEqual(exp, "")));
                }
            }
            if (filter.get_in() != null) {
                List<String> parts = convert(filter.get_in(), Object::toString);
                Predicate predicate = null;
                if (!parts.isEmpty()) {
                    predicate = exp.in(parts);
                }
                if (filter.get_in().contains(null)) {
                    predicates.add(or(crit, crit.isNull(exp), predicate));
                } else if (predicate != null) {
                    predicates.add(predicate);
                }
            }
            if (filter.get_nin() != null) {
                List<String> parts = convert(filter.get_nin(), Object::toString);
                Predicate predicate = null;
                if (!parts.isEmpty()) {
                    predicate = crit.not(exp.in(parts));
                }
                if (filter.get_nin().contains(null)) {
                    predicates.add(or(crit, crit.isNotNull(exp), predicate));
                } else if (predicate != null) {
                    predicates.add(predicate);
                }
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
        private Integer parse(Object o) {
            return o == null ? null : Integer.parseInt(o.toString());
        }

        @Override
        public void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, QueryFilter filter) {
            if (filter.get_eq() != null) {
                ops.add(Expressions.numberOperation(Integer.class, Ops.EQ, path).eq(parse(filter.get_eq())));
            }
            if (filter.get_ne() != null) {
                ops.add(Expressions.numberOperation(Integer.class, Ops.NOT, path).ne(parse(filter.get_ne())));
            }
            if (filter.get_in() != null) {
                List<Integer> parts = convert(filter.get_in(), this::parse);
                BooleanExpression exp = null;
                if (!parts.isEmpty()) {
                    exp = Expressions.numberOperation(Integer.class, Ops.IN, path).in(parts);
                }

                if (filter.get_in().contains(null)) {
                    exp = or(path, path.isNull(), exp);
                }
                if (exp != null) {
                    ops.add(exp);
                }
            }
            if (filter.get_nin() != null) {
                List<Integer> parts = convert(filter.get_nin(), this::parse);
                BooleanExpression exp = null;
                if (!parts.isEmpty()) {
                    exp = Expressions.numberOperation(Integer.class, Ops.NOT_IN, path).notIn(parts);
                }

                if (filter.get_nin().contains(null)) {
                    exp = or(path, path.isNotNull(), exp);
                }
                if (exp != null) {
                    ops.add(exp);
                }
            }
            if (filter.get_lt() != null) {
                ops.add(Expressions.numberOperation(Integer.class, Ops.LT, path).lt(parse(filter.get_lt())));
            }
            if (filter.get_lte() != null) {
                ops.add(Expressions.numberOperation(Integer.class, Ops.LOE, path).loe(parse(filter.get_lte())));
            }
            if (filter.get_gt() != null) {
                ops.add(Expressions.numberOperation(Integer.class, Ops.GT, path).gt(parse(filter.get_gt())));
            }
            if (filter.get_gte() != null) {
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
            if (filter.get_eq() != null) {
                predicates.add(crit.equal(exp, parse(filter.get_eq())));
            }
            if (filter.get_ne() != null) {
                predicates.add(crit.notEqual(exp, parse(filter.get_ne())));
            }
            if (filter.get_in() != null) {
                List<Integer> parts = convert(filter.get_in(), this::parse);
                Predicate predicate = null;
                if (!parts.isEmpty()) {
                    predicate = exp.in(parts);
                }
                if (filter.get_in().contains(null)) {
                    predicates.add(or(crit, crit.isNull(exp), predicate));
                } else if (predicate != null) {
                    predicates.add(predicate);
                }
            }
            if (filter.get_nin() != null) {
                List<Integer> parts = convert(filter.get_nin(), this::parse);
                Predicate predicate = null;
                if (!parts.isEmpty()) {
                    predicate = crit.not(exp.in(parts));
                }
                if (filter.get_nin().contains(null)) {
                    predicates.add(or(crit, crit.isNotNull(exp), predicate));
                } else if (predicate != null) {
                    predicates.add(predicate);
                }
            }
            if (filter.get_lt() != null) {
                predicates.add(crit.lessThan(exp, parse(filter.get_lt())));
            }
            if (filter.get_lte() != null) {
                predicates.add(crit.le(exp, parse(filter.get_lte())));
            }
            if (filter.get_gt() != null) {
                predicates.add(crit.greaterThan(exp, parse(filter.get_gt())));
            }
            if (filter.get_gte() != null) {
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
        private Double parse(Object o) {
            return o == null ? null : Double.parseDouble(o.toString());
        }

        @Override
        public void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, QueryFilter filter) {
            if (filter.get_eq() != null) {
                ops.add(Expressions.numberOperation(Double.class, Ops.EQ, path).eq(parse(filter.get_eq())));
            }
            if (filter.get_ne() != null) {
                ops.add(Expressions.numberOperation(Double.class, Ops.NOT, path).ne(parse(filter.get_ne())));
            }
            if (filter.get_in() != null) {
                List<Double> parts = convert(filter.get_in(), this::parse);
                BooleanExpression exp = null;
                if (!parts.isEmpty()) {
                    exp = Expressions.numberOperation(Double.class, Ops.IN, path).in(parts);
                }

                if (filter.get_in().contains(null)) {
                    exp = or(path, path.isNull(), exp);
                }
                if (exp != null) {
                    ops.add(exp);
                }
            }
            if (filter.get_nin() != null) {
                List<Double> parts = convert(filter.get_nin(), this::parse);
                BooleanExpression exp = null;
                if (!parts.isEmpty()) {
                    exp = Expressions.numberOperation(Double.class, Ops.NOT_IN, path).notIn(parts);
                }

                if (filter.get_nin().contains(null)) {
                    exp = or(path, path.isNotNull(), exp);
                }
                if (exp != null) {
                    ops.add(exp);
                }
            }
            if (filter.get_lt() != null) {
                ops.add(Expressions.numberOperation(Double.class, Ops.LT, path).lt(parse(filter.get_lt())));
            }
            if (filter.get_lte() != null) {
                ops.add(Expressions.numberOperation(Double.class, Ops.LOE, path).loe(parse(filter.get_lte())));
            }
            if (filter.get_gt() != null) {
                ops.add(Expressions.numberOperation(Double.class, Ops.GT, path).gt(parse(filter.get_gt())));
            }
            if (filter.get_gte() != null) {
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
            if (filter.get_eq() != null) {
                predicates.add(crit.equal(exp, parse(filter.get_eq())));
            }
            if (filter.get_ne() != null) {
                predicates.add(crit.notEqual(exp, parse(filter.get_ne())));
            }
            if (filter.get_in() != null) {
                List<Double> parts = convert(filter.get_in(), this::parse);
                Predicate predicate = null;
                if (!parts.isEmpty()) {
                    predicate = exp.in(parts);
                }
                if (filter.get_in().contains(null)) {
                    predicates.add(or(crit, crit.isNull(exp), predicate));
                } else if (predicate != null) {
                    predicates.add(predicate);
                }
            }
            if (filter.get_nin() != null) {
                List<Double> parts = convert(filter.get_nin(), this::parse);
                Predicate predicate = null;
                if (!parts.isEmpty()) {
                    predicate = crit.not(exp.in(parts));
                }
                if (filter.get_nin().contains(null)) {
                    predicates.add(or(crit, crit.isNotNull(exp), predicate));
                } else if (predicate != null) {
                    predicates.add(predicate);
                }
            }
            if (filter.get_lt() != null) {
                predicates.add(crit.lessThan(exp, parse(filter.get_lt())));
            }
            if (filter.get_lte() != null) {
                predicates.add(crit.le(exp, parse(filter.get_lte())));
            }
            if (filter.get_gt() != null) {
                predicates.add(crit.greaterThan(exp, parse(filter.get_gt())));
            }
            if (filter.get_gte() != null) {
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
        private Boolean parse(Object o) {
            if (o == null || o.toString() == null) {
                return null;
            }

            if (o.toString() != null) {
                final String text = o.toString();
                if ("true".equalsIgnoreCase(text)) {
                    return true;
                } else if ("false".equalsIgnoreCase(text)) {
                    return false;
                } else if ("1".equals(text)) {
                    return true;
                } else if ("0".equals(text)) {
                    return false;
                } else if ("null".equalsIgnoreCase(text)) {
                    return null;
                }
            }

            return Boolean.parseBoolean(o.toString());
        }

        @Override
        public void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, QueryFilter filter) {
            if (filter.get_eq() != null) {
                ops.add(Expressions.booleanOperation(Ops.EQ, path).eq(parse(filter.get_eq())));
            }
            if (filter.get_ne() != null) {
                ops.add(Expressions.booleanOperation(Ops.NOT, path).ne(parse(filter.get_ne())));
            }
            if (filter.get_in() != null) {
                List<Boolean> parts = convert(filter.get_in(), this::parse);
                BooleanExpression exp = null;
                if (!parts.isEmpty()) {
                    exp = Expressions.booleanOperation(Ops.IN, path).in(parts);
                }

                if (filter.get_in().contains(null)) {
                    exp = or(path, path.isNull(), exp);
                }
                if (exp != null) {
                    ops.add(exp);
                }
            }
            if (filter.get_nin() != null) {
                List<Boolean> parts = convert(filter.get_nin(), this::parse);
                BooleanExpression exp = null;
                if (!parts.isEmpty()) {
                    exp = Expressions.booleanOperation(Ops.NOT_IN, path).notIn(parts);
                }

                if (filter.get_nin().contains(null)) {
                    exp = or(path, path.isNotNull(), exp);
                }
                if (exp != null) {
                    ops.add(exp);
                }
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
            if (filter.get_eq() != null) {
                predicates.add(crit.equal(exp, parse(filter.get_eq())));
            }
            if (filter.get_ne() != null) {
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
        private Date parse(Object o) {
            return DateUtils.tryParse(o.toString());
        }

        @Override
        public void fillOperations(List<BooleanExpression> ops, PathBuilder<Object> path, QueryFilter filter) {
            if (filter.get_eq() != null) {
                ops.add(Expressions.dateOperation(Date.class, Ops.EQ, path).eq(parse(filter.get_eq())));
            }
            if (filter.get_ne() != null) {
                ops.add(Expressions.dateOperation(Date.class, Ops.NOT, path).ne(parse(filter.get_ne())));
            }
            if (filter.get_in() != null) {
                List<Date> parts = convert(filter.get_in(), this::parse);
                BooleanExpression exp = null;
                if (!parts.isEmpty()) {
                    exp = Expressions.dateOperation(Date.class, Ops.IN, path).in(parts);
                }

                if (filter.get_in().contains(null)) {
                    exp = or(path, path.isNull(), exp);
                }
                if (exp != null) {
                    ops.add(exp);
                }
            }
            if (filter.get_nin() != null) {
                List<Date> parts = convert(filter.get_nin(), this::parse);
                BooleanExpression exp = null;
                if (!parts.isEmpty()) {
                    exp = Expressions.dateOperation(Date.class, Ops.NOT_IN, path).notIn(parts);
                }

                if (filter.get_nin().contains(null)) {
                    exp = or(path, path.isNotNull(), exp);
                }
                if (exp != null) {
                    ops.add(exp);
                }
            }
            if (filter.get_lt() != null) {
                ops.add(Expressions.dateOperation(Date.class, Ops.LT, path).lt(parse(filter.get_lt())));
            }
            if (filter.get_lte() != null) {
                ops.add(Expressions.dateOperation(Date.class, Ops.LOE, path).loe(parse(filter.get_lte())));
            }
            if (filter.get_gt() != null) {
                ops.add(Expressions.dateOperation(Date.class, Ops.GT, path).gt(parse(filter.get_gt())));
            }
            if (filter.get_gte() != null) {
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
            if (filter.get_eq() != null) {
                predicates.add(crit.equal(exp, parse(filter.get_eq())));
            }
            if (filter.get_ne() != null) {
                predicates.add(crit.notEqual(exp, parse(filter.get_ne())));
            }
            if (filter.get_in() != null) {
                List<Date> parts = convert(filter.get_in(), this::parse);
                Predicate predicate = null;
                if (!parts.isEmpty()) {
                    predicate = exp.in(parts);
                }
                if (filter.get_in().contains(null)) {
                    predicates.add(or(crit, crit.isNull(exp), predicate));
                } else if (predicate != null) {
                    predicates.add(predicate);
                }
            }
            if (filter.get_nin() != null) {
                List<Date> parts = convert(filter.get_nin(), this::parse);
                Predicate predicate = null;
                if (!parts.isEmpty()) {
                    predicate = crit.not(exp.in(parts));
                }
                if (filter.get_nin().contains(null)) {
                    predicates.add(or(crit, crit.isNotNull(exp), predicate));
                } else if (predicate != null) {
                    predicates.add(predicate);
                }
            }
            if (filter.get_lt() != null) {
                predicates.add(crit.lessThan(exp, parse(filter.get_lt())));
            }
            if (filter.get_lte() != null) {
                predicates.add(crit.lessThanOrEqualTo(exp, parse(filter.get_lte())));
            }
            if (filter.get_gt() != null) {
                predicates.add(crit.greaterThan(exp, parse(filter.get_gt())));
            }
            if (filter.get_gte() != null) {
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
