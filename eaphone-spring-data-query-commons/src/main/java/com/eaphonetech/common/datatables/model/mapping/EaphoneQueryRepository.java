package com.eaphonetech.common.datatables.model.mapping;

import java.io.Serializable;
import java.util.function.Function;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface EaphoneQueryRepository<T, ID extends Serializable>
		extends PagingAndSortingRepository<T, ID>, CrudRepository<T, ID> {

	/**
	 * Returns the filtered list for the given {@link QueryInput}.
	 * 
	 * @param input the {@link QueryInput} mapped from the Ajax request
	 * @return a {@link QueryOutput}
	 */
	QueryOutput<T> findAll(QueryInput input);

	/**
	 * Returns the filtered list for the given {@link QueryInput}.
	 *
	 * @param <View> generic
	 * @param input the {@link QueryInput} mapped from the Ajax request
	 * @param converter the {@link Function} to apply to the results of the query
	 * @return a {@link QueryOutput}
	 */
	<View> QueryOutput<View> findAll(QueryInput input, Function<T, View> converter);

	/**
	 * Returns the filtered count for the given {@link CountInput}.
	 * 
	 * @param input the {@link CountInput} mapped from the Ajax request
	 * @return a {@link CountOutput}
	 */
	CountOutput count(CountInput input);

}
