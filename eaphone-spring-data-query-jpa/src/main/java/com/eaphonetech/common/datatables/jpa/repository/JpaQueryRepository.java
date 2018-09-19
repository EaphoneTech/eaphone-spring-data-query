package com.eaphonetech.common.datatables.jpa.repository;

import java.io.Serializable;
import java.util.function.Function;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.eaphonetech.common.datatables.model.mapping.EaphoneQueryRepository;
import com.eaphonetech.common.datatables.model.mapping.QueryInput;
import com.eaphonetech.common.datatables.model.mapping.QueryOutput;

/**
 * Convenience interface to allow pulling in {@link PagingAndSortingRepository} and
 * {@link JpaSpecificationExecutor} functionality in one go.
 * 
 * @author Damien Arrachequesne
 */
@NoRepositoryBean
public interface JpaQueryRepository<T, ID extends Serializable>
        extends EaphoneQueryRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * Returns the filtered list for the given {@link QueryInput}.
     * 
     * @param input the {@link QueryInput} mapped from the Ajax request
     * @param additionalSpecification an additional {@link Specification} to apply to the query (with
     *            an "AND" clause)
     * @return a {@link QueryOutput}
     */
    QueryOutput<T> findAll(QueryInput input, Specification<T> additionalSpecification);

    /**
     * Returns the filtered list for the given {@link QueryInput}.
     * 
     * @param input the {@link QueryInput} mapped from the Ajax request
     * @param additionalSpecification an additional {@link Specification} to apply to the query (with
     *            an "AND" clause)
     * @param preFilteringSpecification a pre-filtering {@link Specification} to apply to the query
     *            (with an "AND" clause)
     * @return a {@link QueryOutput}
     */
    QueryOutput<T> findAll(QueryInput input, Specification<T> additionalSpecification,
            Specification<T> preFilteringSpecification);

    /**
     * Returns the filtered list for the given {@link QueryInput}.
     *
     * @param input the {@link QueryInput} mapped from the Ajax request
     * @param additionalSpecification an additional {@link Specification} to apply to the query (with
     *            an "AND" clause)
     * @param preFilteringSpecification a pre-filtering {@link Specification} to apply to the query
     *            (with an "AND" clause)
     * @param converter the {@link Function} to apply to the results of the query
     * @return a {@link QueryOutput}
     */
    <R> QueryOutput<R> findAll(QueryInput input, Specification<T> additionalSpecification,
            Specification<T> preFilteringSpecification, Function<T, R> converter);

}
