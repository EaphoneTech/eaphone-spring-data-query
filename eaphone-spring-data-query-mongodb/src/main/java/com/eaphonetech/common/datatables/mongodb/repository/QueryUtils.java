package com.eaphonetech.common.datatables.mongodb.repository;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.skip;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.reflections.ReflectionUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.util.StringUtils;

import com.eaphonetech.common.datatables.model.mapping.ColumnType;
import com.eaphonetech.common.datatables.model.mapping.CountInput;
import com.eaphonetech.common.datatables.model.mapping.QueryInput;
import com.eaphonetech.common.datatables.model.mapping.filter.QueryFilter;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueryUtils {
    /**
     * check jackson at startup
     */
    private static boolean IS_JACKSON_AVAILABLE = false;
    static {
        try {
            Class.forName("com.fasterxml.jackson.annotation.JsonProperty");
            IS_JACKSON_AVAILABLE = true;
        } catch (ClassNotFoundException cnfe) {
        }
    }

    public static <T, ID extends Serializable> Query getQuery(MongoEntityInformation<T, ID> entityInformation,
            final QueryInput input) {
        Query q = new Query();
        List<Criteria> criteriaList = getCriteria(input, entityInformation);
        if (criteriaList != null) {
            for (final Criteria c : criteriaList) {
                q.addCriteria(c);
            }
        }
        return q;
    }

    private static List<Object> convertArray(ColumnType type, List<Object> value) {
        return value.stream().filter(Objects::nonNull).map(o -> type.tryConvert(o)).collect(Collectors.toList());
    }

    private static class ColumnTypeAndName {
        private ColumnType type;
        private String name;
    }

    /**
     * Find one possible field of nested name parts.
     * 
     * @param javaType
     * @param fieldNameParts
     * @param currentIndex
     * @return Never return <code>null</code>.
     */
    private static ColumnTypeAndName getField(Class<?> javaType, String[] fieldNameParts, int currentIndex) {
        Objects.requireNonNull(fieldNameParts);
        ColumnTypeAndName result = new ColumnTypeAndName();
        if (javaType == null) {
            return result;
        }

        if (currentIndex <= fieldNameParts.length - 1) {
            final String currentLevelName = fieldNameParts[currentIndex];
            String decidedName = null;
            Class<?> currentLevelFieldType = null;

            // do logic and append more
            @SuppressWarnings("unchecked")
            Set<Field> possibleFields = ReflectionUtils.getAllFields(javaType, input -> {
                if (input != null) {
                    if (currentLevelName.equals(input.getName())) {
                        return true;
                    } else if (IS_JACKSON_AVAILABLE) {
                        // direct matching with @JsonProperty
                        final JsonProperty jsonProperty = input.getAnnotation(JsonProperty.class);
                        if (jsonProperty != null && StringUtils.hasLength(jsonProperty.value())) {
                            if (currentLevelName.equals(jsonProperty.value())) {
                                return true;
                            }
                        }

                        // TODO: Jackson PropertyNamingStrategy should also be considered
                    }
                }
                return false;
            });

            if (possibleFields != null && !possibleFields.isEmpty()) {
                for (final Field field : possibleFields) {
                    decidedName = field.getName();
                    // TODO: currentLevelFieldType = List<T>, should be T, got List
                    currentLevelFieldType = field.getType();

                    try {
                        Type t = field.getGenericType();
                        if (t != null && t instanceof ParameterizedType) {
                            ParameterizedType pti = (ParameterizedType) t;
                            Type tt = pti.getActualTypeArguments()[0];
                            if (tt instanceof Class) {
                                currentLevelFieldType = (Class<?>) tt;
                            }
                        }
                    } catch (Exception ex) {
                        log.debug("Caught unhandled exception", ex);
                    }
                    break;
                }
            }

            if (currentLevelFieldType == null) {
                return result;
            }

            if (currentIndex < fieldNameParts.length - 1) {
                ColumnTypeAndName childrenField = getField(currentLevelFieldType, fieldNameParts, currentIndex + 1);
                if (childrenField == null) {
                    result.type = null;
                    result.name = decidedName;
                } else {
                    result.type = childrenField.type;
                    result.name = decidedName + "." + childrenField.name;
                }
            } else {
                result.type = ColumnType.parse(currentLevelFieldType);
                result.name = decidedName;
            }
        }
        return result;
    }

    /**
     * Recursively get field type
     * 
     * @param javaType
     * @param fieldNameParts
     * @param currentIndex
     * @return
     */
    private static ColumnType getFieldType(Class<?> javaType, String[] fieldNameParts, int currentIndex) {
        return getField(javaType, fieldNameParts, currentIndex).type;
    }

    /**
     * Determine actual MongoDB field type from input
     * (including jackson-databind, etc.)
     * 
     * @param javaType actual java type
     * @param fieldName frontend provided field name
     * @return
     */
    private static ColumnType getFieldType(Class<?> javaType, String fieldName) {
        if (javaType == null || StringUtils.isEmpty(fieldName)) {
            return null;
        }

        ColumnType result = null;
        if (fieldName.contains(".")) {
            final String[] parts = fieldName.split("\\.");
            result = getFieldType(javaType, parts, 0);
        } else {
            result = getFieldType(javaType, new String[] { fieldName }, 0);
        }
        log.trace("getFieldType({}, '{}') returns : '{}'", javaType.getSimpleName(), fieldName, result);
        return result;
    }

    /**
     * Recursively get field name
     * 
     * @param javaType
     * @param fieldNameParts
     * @param currentIndex
     * @return
     */
    private static String getFieldName(Class<?> javaType, String[] fieldNameParts, int currentIndex) {
        return getField(javaType, fieldNameParts, currentIndex).name;
    }

    /**
     * Determine actual MongoDB field name from input
     * (including jackson-databind, etc.)
     * 
     * @param javaType actual java type
     * @param fieldName frontend provided field name
     * @return
     */
    private static String getFieldName(Class<?> javaType, String fieldName) {
        if (javaType == null || StringUtils.isEmpty(fieldName)) {
            return null;
        }

        String result = null;
        if (fieldName.contains(".")) {
            final String[] parts = fieldName.split("\\.");
            result = getFieldName(javaType, parts, 0);
        } else {
            result = getFieldName(javaType, new String[] { fieldName }, 0);
        }
        log.trace("getFieldName({}, '{}') returns : '{}'", javaType.getSimpleName(), fieldName, result);
        return result;
    }

    /**
     * Convert a {@link QueryInput} to Criteia
     * 
     * @param input
     * @return
     */
    private static <T, ID extends Serializable> List<Criteria> getCriteria(final QueryInput input,
            MongoEntityInformation<T, ID> entityInformation) {
        List<Criteria> result = new LinkedList<>();
        // check for each searchable column whether a filter value exists
        for (final Map.Entry<String, QueryFilter> entry : input.getWhere().entrySet()) {
            final QueryFilter filter = entry.getValue();
            final String fieldName = entry.getKey();
            final ColumnType type = getFieldType(entityInformation.getJavaType(), fieldName);
            if (type == null) {
                throw new RuntimeException(String.format("field [%s] not exists", fieldName));
            }
            // handle column.filter
            if (filter != null) {
                boolean hasValidCrit = false;
                final String queryFieldName = getFieldName(entityInformation.getJavaType(), fieldName);
                Criteria c = Criteria.where(queryFieldName);
                if (filter.get_eq() != null) {
                    // $eq takes first place
                    c.is(type.tryConvert(filter.get_eq()));
                    hasValidCrit = true;
                } else if (filter.get_ne() != null) {
                    // $ne
                    c.ne(type.tryConvert(filter.get_ne()));
                    hasValidCrit = true;
                } else {
                    if (filter.get_in() != null) {
                        // $in takes second place
                        List<Object> parts = convertArray(type, filter.get_in());
                        if (filter.get_in().contains(null)) {
                            if (parts.isEmpty()) {
                                c.is(null);
                                hasValidCrit = true;
                            } else {
                                // 此时不改变 c 的有效性
                                result.add(new Criteria().orOperator(
                                        Criteria.where(queryFieldName).is(null),
                                        Criteria.where(queryFieldName).in(parts)));
                            }
                        } else {
                            c.in(parts);
                            hasValidCrit = true;
                        }
                    }

                    if (filter.get_nin() != null) {
                        List<Object> parts = convertArray(type, filter.get_nin());
                        if (filter.get_nin().contains(null)) {
                            if (parts.isEmpty()) {
                                c.ne(null);
                                hasValidCrit = true;
                            } else {
                                result.add(new Criteria().orOperator(
                                        Criteria.where(queryFieldName).ne(null),
                                        Criteria.where(queryFieldName).nin(parts)));
                            }
                        } else {
                            c.nin(parts);
                            hasValidCrit = true;
                        }
                    }

                    if (StringUtils.hasLength(filter.get_regex())) {
                        // $regex also works here
                        c.regex(filter.get_regex());
                        hasValidCrit = true;
                    } else if (StringUtils.hasLength(filter.get_like())) {
                        // like is converted to $regex
                        c.regex(getLikeFilterPattern(filter.get_like()));
                        hasValidCrit = true;
                    }

                    if (filter.get_null() != null && filter.get_null().booleanValue()) {
                        c.is(null);
                        hasValidCrit = true;
                    }

                    if (filter.get_empty() != null && filter.get_empty().booleanValue()) {
                        c.is("");
                        hasValidCrit = true;
                    }

                    if (filter.get_exists() != null) {
                        c.exists(filter.get_exists().booleanValue());
                        hasValidCrit = true;
                    }

                    if (type.isComparable()) {
                        // $gt, $lt, etc. only works if type is comparable
                        if (filter.get_gt() != null) {
                            c.gt(type.tryConvert(filter.get_gt()));
                            hasValidCrit = true;
                        }
                        if (filter.get_gte() != null) {
                            c.gte(type.tryConvert(filter.get_gte()));
                            hasValidCrit = true;
                        }
                        if (filter.get_lt() != null) {
                            c.lt(type.tryConvert(filter.get_lt()));
                            hasValidCrit = true;
                        }
                        if (filter.get_lte() != null) {
                            c.lte(type.tryConvert(filter.get_lte()));
                            hasValidCrit = true;
                        }
                    }
                }
                if (hasValidCrit) {
                    result.add(c);
                }
            }
        }

        return result;
    }

    /**
     * Creates a '$sort' clause for the given {@link QueryInput}.
     * 
     * @param <T> generic
     * @param <ID> generic
     * @param entityInformation {@link MongoEntityInformation}
     * @param input the {@link QueryInput} mapped from the Ajax request
     * @return a {@link Pageable}, must not be {@literal null}.
     */
    public static <T, ID extends Serializable> Pageable getPageable(MongoEntityInformation<T, ID> entityInformation,
            QueryInput input) {
        List<Order> orders = input.getOrders();

        // handle JsonProperty annotation
        Sort sort = orders.isEmpty() ? Sort.unsorted()
                : Sort.by(orders.stream()
                        .map(o -> new Sort.Order(o.getDirection(),
                                Optional.ofNullable(getFieldName(entityInformation.getJavaType(), o.getProperty()))
                                        .orElse(o.getProperty())))
                        .collect(Collectors.toList()));

        if (input.getLimit() == -1) {
            input.setLimit(Integer.MAX_VALUE);
        }
        return PageRequest.of(input.getOffset() / input.getLimit(), input.getLimit(), sort);
    }

    /**
     * "LIKE" search is converted to $regex
     * 
     * @param filterValue filter value
     * @return
     */
    public static Pattern getLikeFilterPattern(String filterValue) {
        if (filterValue == null) {
            return null;
        }
        String pattern = filterValue;
        // escape regex symbols
        for (char t : ".+?*^$()[]".toCharArray()) {
            pattern = pattern.replaceAll("\\" + t, "\\\\" + t);
        }
        // handle start and end
        if (!filterValue.startsWith("%")) {
            pattern = "^" + pattern;
        }
        if (!filterValue.endsWith("%")) {
            pattern = pattern + "$";
        }
        pattern = pattern.replaceAll("%", ".*");
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Convert {@link QueryInput} to {@link AggregationOperation}[], mainly for column searches.
     * 
     * @param input
     * @return
     */
    private static <T, ID extends Serializable> List<AggregationOperation> toAggregationOperation(
            MongoEntityInformation<T, ID> entityInformation, QueryInput input) {
        List<AggregationOperation> result = new LinkedList<>();
        List<Criteria> criteriaList = getCriteria(input, entityInformation);
        if (criteriaList != null) {
            for (final Criteria c : criteriaList) {
                result.add(match(c));
            }
        }
        return result;
    }

    /**
     * Create an {@link TypedAggregation} with specified {@link QueryInput} as filter, plus specified
     * {@link AggregationOperation}[], but only act as <code>$count</code>
     * <p>This basically creates an aggregation pipeline as follows:</p>
     * 
     * <pre>
     * <code>
     * [
     *      ...operations,
     *      {$group: {"_id": null, "_count": {$sum: 1}}}
     * ]
     * </code>
     * </pre>
     * 
     * @param entityInformation {@link MongoEntityInformation}
     * @param input the {@link QueryInput} mapped from the Ajax request
     * @param operations aggregation operations
     * @return a {@link TypedAggregation}
     */
    public static <T, ID extends Serializable> TypedAggregation<T> makeAggregationCountOnly(
            MongoEntityInformation<T, ID> entityInformation, QueryInput input, AggregationOperation[] operations) {
        List<AggregationOperation> opList = new LinkedList<>();
        if (operations != null) {
            for (int i = 0; i < operations.length; i++) {
                opList.add(operations[i]);
            }
        }

        opList.addAll(toAggregationOperation(entityInformation, input));

        opList.add(group().count().as("_count"));
        return newAggregation(entityInformation.getJavaType(), opList);
    }

    /**
     * Create an {@link TypedAggregation} with specified {@link QueryInput} as filter, plus specified
     * {@link AggregationOperation}[]
     * 
     * @param classOfT
     * @param input
     * @param pageable
     * @param operations
     * @return
     */
    public static <T> TypedAggregation<T> makeAggregation(Class<T> classOfT, QueryInput input, Pageable pageable,
            AggregationOperation[] operations) {
        List<AggregationOperation> opList = new LinkedList<>();
        if (operations != null) {
            for (int i = 0; i < operations.length; i++) {
                opList.add(operations[i]);
            }
        }

        opList.addAll(toAggregationOperation(null, input));

        if (pageable != null) {
            final Sort s = pageable.getSort();
            if (s != null && !s.isUnsorted()) {
                opList.add(sort(s));
            }
            opList.add(skip((long) pageable.getOffset()));
            opList.add(limit(pageable.getPageSize()));
        }
        return newAggregation(classOfT, opList);
    }

    /**
     * 
     * @param <T>
     * @param <ID>
     * @param entityInformation
     * @param input
     * @return
     */
    public static <T, ID extends Serializable> Query getQuery(MongoEntityInformation<T, ID> entityInformation,
            CountInput input) {
        Query q = new Query();
        List<Criteria> criteriaList = getCriteria(input, entityInformation);
        if (criteriaList != null) {
            for (final Criteria c : criteriaList) {
                q.addCriteria(c);
            }
        }
        return q;
    }

    /**
     * 
     * @param input
     * @param entityInformation
     * @return
     */
    private static <T, ID extends Serializable> List<Criteria> getCriteria(CountInput input,
            MongoEntityInformation<T, ID> entityInformation) {
        List<Criteria> result = new LinkedList<>();
        // check for each searchable column whether a filter value exists
        for (final Map.Entry<String, QueryFilter> entry : input.entrySet()) {
            final QueryFilter filter = entry.getValue();
            final String fieldName = entry.getKey();
            final ColumnType type = getFieldType(entityInformation.getJavaType(), fieldName);
            if (type == null) {
                throw new RuntimeException(String.format("field [%s] not exists", fieldName));
            }
            // handle column.filter
            if (filter != null) {
                boolean hasValidCrit = false;
                Criteria c = Criteria.where(getFieldName(entityInformation.getJavaType(), fieldName));
                if (filter.get_eq() != null) {
                    // $eq takes first place
                    c.is(type.tryConvert(filter.get_eq()));
                    hasValidCrit = true;
                } else if (filter.get_ne() != null) {
                    // $ne
                    c.ne(type.tryConvert(filter.get_ne()));
                    hasValidCrit = true;
                } else {
                    if (filter.get_in() != null) {
                        // $in takes second place
                        c.in(convertArray(type, filter.get_in()));
                        hasValidCrit = true;
                    }

                    if (filter.get_nin() != null) {
                        c.nin(convertArray(type, filter.get_nin()));
                        hasValidCrit = true;
                    }

                    if (StringUtils.hasLength(filter.get_regex())) {
                        // $regex also works here
                        c.regex(filter.get_regex());
                        hasValidCrit = true;
                    } else if (StringUtils.hasLength(filter.get_like())) {
                        // like is converted to $regex
                        c.regex(getLikeFilterPattern(filter.get_like()));
                        hasValidCrit = true;
                    }

                    if (filter.get_null() != null && filter.get_null().booleanValue()) {
                        c.is(null);
                        hasValidCrit = true;
                    }

                    if (filter.get_empty() != null && filter.get_empty().booleanValue()) {
                        c.is("");
                        hasValidCrit = true;
                    }

                    if (filter.get_exists() != null) {
                        c.exists(filter.get_exists().booleanValue());
                        hasValidCrit = true;
                    }

                    if (type.isComparable()) {
                        // $gt, $lt, etc. only works if type is comparable
                        if (filter.get_gt() != null) {
                            c.gt(type.tryConvert(filter.get_gt()));
                            hasValidCrit = true;
                        }
                        if (filter.get_gte() != null) {
                            c.gte(type.tryConvert(filter.get_gte()));
                            hasValidCrit = true;
                        }
                        if (filter.get_lt() != null) {
                            c.lt(type.tryConvert(filter.get_lt()));
                            hasValidCrit = true;
                        }
                        if (filter.get_lte() != null) {
                            c.lte(type.tryConvert(filter.get_lte()));
                            hasValidCrit = true;
                        }
                    }
                }
                if (hasValidCrit) {
                    result.add(c);
                }
            }
        }

        return result;
    }

}
