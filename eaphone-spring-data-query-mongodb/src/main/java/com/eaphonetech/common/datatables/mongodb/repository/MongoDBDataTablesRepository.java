package com.eaphonetech.common.datatables.mongodb.repository;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.repository.NoRepositoryBean;

import com.eaphonetech.common.datatables.model.mapping.DataTablesInput;
import com.eaphonetech.common.datatables.model.mapping.DataTablesOutput;
import com.eaphonetech.common.datatables.model.mapping.DataTablesRepository;

@NoRepositoryBean
public interface MongoDBDataTablesRepository<T, ID extends Serializable> extends DataTablesRepository<T, ID> {

    /**
     * Returns the filtered list for the given {@link DataTablesInput}.
     *
     * @param input the {@link DataTablesInput} mapped from the Ajax request
     * @return a {@link DataTablesOutput}
     */
    DataTablesOutput<T> findAll(DataTablesInput input);

    /**
     * Returns the filtered list for the given {@link DataTablesInput}.
     *
     * @param input the {@link DataTablesInput} mapped from the Ajax request
     * @param additionalSpecification an additional {@link Criteria} to apply to the query
     *            (with an "AND" clause)
     * @return a {@link DataTablesOutput}
     */
    DataTablesOutput<T> findAll(DataTablesInput input, Criteria additionalCriteria);

    /**
     * Returns the filtered list for the given {@link DataTablesInput}.
     *
     * @param input the {@link DataTablesInput} mapped from the Ajax request
     * @param additionalSpecification an additional {@link Criteria} to apply to the query
     *            (with an "AND" clause)
     * @param preFilteringSpecification a pre-filtering {@link Criteria} to apply to the query
     *            (with an "AND" clause)
     * @return a {@link DataTablesOutput}
     */
    DataTablesOutput<T> findAll(DataTablesInput input, Criteria additionalCriteria, Criteria preFilteringCriteria);

    /**
     * Returns the filtered list for the given {@link DataTablesInput} using the given {@link TypedAggregation}
     * 
     * @param classOfView
     * @param input
     * @param operations
     * @return
     */
    <View> DataTablesOutput<View> findAll(Class<View> classOfView, DataTablesInput input,
            AggregationOperation... operations);

    /**
     * Returns the filtered list for the given {@link DataTablesInput} using the given {@link TypedAggregation}
     * 
     * @param classOfView
     * @param input
     * @param operations
     * @return
     */
    <View> DataTablesOutput<View> findAll(Class<View> classOfView, DataTablesInput input,
            Collection<? extends AggregationOperation> operations);

}
