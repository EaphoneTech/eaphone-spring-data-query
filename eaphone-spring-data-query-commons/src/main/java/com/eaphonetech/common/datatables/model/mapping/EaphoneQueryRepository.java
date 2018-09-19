package com.eaphonetech.common.datatables.model.mapping;

import java.io.Serializable;
import java.util.function.Function;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface EaphoneQueryRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {

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
     * @param input the {@link QueryInput} mapped from the Ajax request
     * @param converter the {@link Function} to apply to the results of the query
     * @return a {@link QueryOutput}
     */
    <R> QueryOutput<R> findAll(QueryInput input, Function<T, R> converter);
}
