package com.eaphonetech.common.datatables.jpa.repository;

import java.io.Serializable;
import java.util.function.Function;

import javax.persistence.EntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import com.eaphonetech.common.datatables.jpa.SpecificationBuilder;
import com.eaphonetech.common.datatables.model.mapping.QueryInput;
import com.eaphonetech.common.datatables.model.mapping.QueryOutput;
import com.eaphonetech.common.datatables.util.Converter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EaphoneQueryRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
        implements JpaQueryRepository<T, ID> {

    EaphoneQueryRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Override
    public QueryOutput<T> findAll(QueryInput input) {
        return findAll(input, null, null);
    }

    @Override
    public QueryOutput<T> findAll(QueryInput input, Specification<T> additionalSpecification) {
        return findAll(input, additionalSpecification, null);
    }

    @Override
    public QueryOutput<T> findAll(QueryInput input, Specification<T> additionalSpecification,
            Specification<T> preFilteringSpecification) {
        QueryOutput<T> output = new QueryOutput<>();
        if (input.getLimit() == 0) {
            return output;
        }

        try {
            long recordsTotal = preFilteringSpecification == null ? count() : count(preFilteringSpecification);
            if (recordsTotal == 0) {
                return output;
            }
            output.setTotal(recordsTotal);

            SpecificationBuilder<T> specificationBuilder = new SpecificationBuilder<>(input);
            Page<T> data = findAll(Specification.where(specificationBuilder.build()).and(additionalSpecification)
                    .and(preFilteringSpecification), specificationBuilder.createPageable());

            output.setDraw(input.getDraw());
            output.setData(data.getContent());
            output.setFiltered(data.getTotalElements());

        } catch (Exception e) {
            output.setDraw(input.getDraw());
            output.setError(e.toString());
            output.setFiltered(0L);
            log.error("caught exception", e);
        }

        return output;
    }

    @Override
    public <R> QueryOutput<R> findAll(QueryInput input, Function<T, R> converter) {
        return findAll(input, null, null, converter);
    }

    @Override
    public <R> QueryOutput<R> findAll(QueryInput input, Specification<T> additionalSpecification,
            Specification<T> preFilteringSpecification, Function<T, R> converter) {
        QueryOutput<T> raw = findAll(input, additionalSpecification, preFilteringSpecification);
        return Converter.convert(raw, converter);
    }

}