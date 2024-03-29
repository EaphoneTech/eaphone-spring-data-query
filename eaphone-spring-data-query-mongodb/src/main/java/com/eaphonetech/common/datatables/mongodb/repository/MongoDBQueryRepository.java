package com.eaphonetech.common.datatables.mongodb.repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Function;

import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.repository.NoRepositoryBean;

import com.eaphonetech.common.datatables.model.mapping.CountInput;
import com.eaphonetech.common.datatables.model.mapping.CountOutput;
import com.eaphonetech.common.datatables.model.mapping.EaphoneQueryRepository;
import com.eaphonetech.common.datatables.model.mapping.QueryInput;
import com.eaphonetech.common.datatables.model.mapping.QueryOutput;

@NoRepositoryBean
public interface MongoDBQueryRepository<T, ID extends Serializable> extends EaphoneQueryRepository<T, ID> {

	/**
	 * Returns the filtered list for the given {@link QueryInput}.
	 *
	 * @param input the {@link QueryInput} mapped from the Ajax request
	 * @param additionalCriteria an additional {@link Criteria} to apply to the query
	 *            (with an "AND" clause)
	 * @return a {@link QueryOutput}
	 */
	QueryOutput<T> findAll(QueryInput input, Criteria additionalCriteria);

	/**
	 * Returns the filtered list for the given {@link QueryInput}.
	 *
	 * @param input the {@link QueryInput} mapped from the Ajax request
	 * @param additionalCriteria an additional {@link Criteria} to apply to the query
	 *            (with an "AND" clause)
	 * @param preFilteringCriteria a pre-filtering {@link Criteria} to apply to the query
	 *            (with an "AND" clause)
	 * @return a {@link QueryOutput}
	 */
	QueryOutput<T> findAll(QueryInput input, Criteria additionalCriteria, Criteria preFilteringCriteria);

	/**
	 * Find with type conversion
	 * @param <View> generic
	 * @param input the {@link QueryInput} mapped from the Ajax request
	 * @param additionalCrit an additional {@link Criteria} to apply to the query
	 * @param preFilteringCrit a pre-filtering {@link Criteria} to apply to the query
	 * @param converter the {@link Function} to apply to the results of the query
	 * @return a {@link QueryOutput}
	 */
	<View> QueryOutput<View> findAll(QueryInput input, Criteria additionalCrit, Criteria preFilteringCrit,
			Function<T, View> converter);

	/**
	 * Returns the filtered list for the given {@link QueryInput} using the given {@link TypedAggregation}
	 * 
	 * @param <View> generic
	 * @param classOfView class of view
	 * @param input the {@link QueryInput} mapped from the Ajax request
	 * @param operations aggregation operations
	 * @return a {@link QueryOutput}
	 */
	<View> QueryOutput<View> findAll(Class<View> classOfView, QueryInput input, AggregationOperation... operations);

	/**
	 * Returns the filtered list for the given {@link QueryInput} using the given {@link TypedAggregation}
	 * 
	 * @param <View> generic
	 * @param classOfView class of view
	 * @param input the {@link QueryInput} mapped from the Ajax request
	 * @param operations aggregation operations
	 * @return a {@link QueryOutput}
	 */
	<View> QueryOutput<View> findAll(Class<View> classOfView, QueryInput input,
			Collection<? extends AggregationOperation> operations);

	/**
	 * Returns the filtered count for the given {@link CountInput}.
	 *
	 * @param input the {@link CountInput} mapped from the Ajax request
	 * @param additionalCriteria an additional {@link Criteria} to apply to the query
	 *            (with an "AND" clause)
	 * @return a {@link CountOutput}
	 */
	CountOutput count(CountInput input, Criteria additionalCriteria);

	/**
	 * Returns the filtered count for the given {@link CountInput}.
	 *
	 * @param input the {@link CountInput} mapped from the Ajax request
	 * @param additionalCriteria an additional {@link Criteria} to apply to the query
	 *            (with an "AND" clause)
	 * @param preFilteringCriteria a pre-filtering {@link Criteria} to apply to the query
	 *            (with an "AND" clause)
	 * @return a {@link CountOutput}
	 */
	CountOutput count(CountInput input, Criteria additionalCriteria, Criteria preFilteringCriteria);

}
