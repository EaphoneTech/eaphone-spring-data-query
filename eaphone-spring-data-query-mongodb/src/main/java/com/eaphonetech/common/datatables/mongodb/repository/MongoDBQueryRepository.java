package com.eaphonetech.common.datatables.mongodb.repository;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.repository.NoRepositoryBean;

import com.eaphonetech.common.datatables.model.mapping.EaphoneQueryRepository;
import com.eaphonetech.common.datatables.model.mapping.QueryInput;
import com.eaphonetech.common.datatables.model.mapping.QueryOutput;

@NoRepositoryBean
public interface MongoDBQueryRepository<T, ID extends Serializable> extends EaphoneQueryRepository<T, ID> {

    /**
     * Returns the filtered list for the given {@link QueryInput}.
     *
     * @param input the {@link QueryInput} mapped from the Ajax request
     * @param additionalSpecification an additional {@link Criteria} to apply to the query
     *            (with an "AND" clause)
     * @return a {@link QueryOutput}
     */
    QueryOutput<T> findAll(QueryInput input, Criteria additionalCriteria);

    /**
     * Returns the filtered list for the given {@link QueryInput}.
     *
     * @param input the {@link QueryInput} mapped from the Ajax request
     * @param additionalSpecification an additional {@link Criteria} to apply to the query
     *            (with an "AND" clause)
     * @param preFilteringSpecification a pre-filtering {@link Criteria} to apply to the query
     *            (with an "AND" clause)
     * @return a {@link QueryOutput}
     */
    QueryOutput<T> findAll(QueryInput input, Criteria additionalCriteria, Criteria preFilteringCriteria);

    /**
     * Returns the filtered list for the given {@link QueryInput} using the given {@link TypedAggregation}
     * 
     * @param classOfView
     * @param input
     * @param operations
     * @return
     */
    <View> QueryOutput<View> findAll(Class<View> classOfView, QueryInput input,
            AggregationOperation... operations);

    /**
     * Returns the filtered list for the given {@link QueryInput} using the given {@link TypedAggregation}
     * 
     * @param classOfView
     * @param input
     * @param operations
     * @return
     */
    <View> QueryOutput<View> findAll(Class<View> classOfView, QueryInput input,
            Collection<? extends AggregationOperation> operations);

}
