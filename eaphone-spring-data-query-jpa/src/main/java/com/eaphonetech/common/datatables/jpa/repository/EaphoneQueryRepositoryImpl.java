package com.eaphonetech.common.datatables.jpa.repository;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

import javax.persistence.EntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import com.eaphonetech.common.datatables.jpa.SpecificationBuilder;
import com.eaphonetech.common.datatables.model.mapping.QueryInput;
import com.eaphonetech.common.datatables.model.mapping.QueryOutput;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EaphoneQueryRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
        implements JpaQueryRepository<T, ID> {

    EaphoneQueryRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {

        super(entityInformation, entityManager);
    }

    @Override
    public QueryOutput<T> findAll(QueryInput input) {
        return findAll(input, null, null, null);
    }

    @Override
    public QueryOutput<T> findAll(QueryInput input, Specification<T> additionalSpecification) {
        return findAll(input, additionalSpecification, null, null);
    }

    @Override
    public QueryOutput<T> findAll(QueryInput input, Specification<T> additionalSpecification,
            Specification<T> preFilteringSpecification) {
        return findAll(input, additionalSpecification, preFilteringSpecification, null);
    }

    @Override
    public <R> QueryOutput<R> findAll(QueryInput input, Function<T, R> converter) {
        return findAll(input, null, null, converter);
    }

    @Override
    public <R> QueryOutput<R> findAll(QueryInput input, Specification<T> additionalSpecification,
            Specification<T> preFilteringSpecification, Function<T, R> converter) {
        QueryOutput<R> output = new QueryOutput<>();
        output.setDraw(input.getDraw());
        if (input.getLength() == 0) {
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

            @SuppressWarnings("unchecked")
            List<R> content = converter == null ? (List<R>) data.getContent() : data.map(converter).getContent();
            output.setData(content);
            output.setFiltered(data.getTotalElements());

        } catch (Exception e) {
            log.error("exception", e);
            output.setError(e.toString());
        }

        return output;
    }

}