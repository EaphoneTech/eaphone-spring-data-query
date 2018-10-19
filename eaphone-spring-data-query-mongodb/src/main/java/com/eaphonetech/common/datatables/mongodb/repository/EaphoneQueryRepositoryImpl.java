package com.eaphonetech.common.datatables.mongodb.repository;

import static org.springframework.data.mongodb.core.query.Query.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;

import com.eaphonetech.common.datatables.model.mapping.QueryInput;
import com.eaphonetech.common.datatables.model.mapping.QueryOutput;
import com.eaphonetech.common.datatables.mongodb.model.QueryCount;

/**
 * Repository implementation
 *
 * @author Xiaoyu Guo
 */
public class EaphoneQueryRepositoryImpl<T, ID extends Serializable> extends SimpleMongoRepository<T, ID>
        implements MongoDBQueryRepository<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(EaphoneQueryRepositoryImpl.class);

    private final MongoEntityInformation<T, ID> entityInformation;
    private final MongoOperations mongoOperations;

    public EaphoneQueryRepositoryImpl(MongoEntityInformation<T, ID> metadata, MongoOperations mongoOperations) {
        super(metadata, mongoOperations);
        this.entityInformation = metadata;
        this.mongoOperations = mongoOperations;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.data.mongodb.datatables.repository.DataTablesRepository#findAll(org.springframework.data.jpa.
     * datatables.mapping.QueryInput)
     */
    @Override
    public QueryOutput<T> findAll(QueryInput input) {
        return findAll(input, null, null, null);
    }

    @Override
    public <R> QueryOutput<R> findAll(QueryInput input, Function<T, R> converter) {
        return findAll(input, null, null, converter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.data.mongodb.datatables.repository.DataTablesRepository#findAll(org.springframework.data.jpa.
     * datatables.mapping.QueryInput, org.springframework.data.mongodb.core.query.Criteria)
     */
    @Override
    public QueryOutput<T> findAll(QueryInput input, Criteria additionalCriteria) {
        return findAll(input, additionalCriteria, null, null);
    }

    private long count(Criteria crit) {
        Query q = query(crit);
        return this.mongoOperations.count(q, this.entityInformation.getCollectionName());
    }

    private <S extends T> Page<S> findAll(Query q, Pageable p, Class<S> classOfS) {
        long count = mongoOperations.count(q, this.entityInformation.getCollectionName());

        if (count == 0) {
            return new PageImpl<S>(Collections.<S> emptyList());
        }
        if (p != null) {
            if (p.getSort() == null) {
                q.limit(p.getPageSize()).skip(p.getOffset());
            } else {
                q.with(p);
            }
        }
        return new PageImpl<S>(mongoOperations.find(q, classOfS, this.entityInformation.getCollectionName()), p, count);
    }

    @Override
    public QueryOutput<T> findAll(QueryInput input, Criteria additionalCriteria, Criteria preFilteringCriteria) {
        return findAll(input, additionalCriteria, preFilteringCriteria, null);
    }

    /**
     * @param input
     * @param additionalCrit
     * @param preFilteringCrit
     * @param converter
     * @return
     */
    public <R> QueryOutput<R> findAll(QueryInput input, Criteria additionalCrit, Criteria preFilteringCrit,
            Function<T, R> converter) {
        QueryOutput<R> output = new QueryOutput<R>();
        output.setDraw(input.getDraw());

        try {
            long recordsTotal = preFilteringCrit == null ? count() : count(preFilteringCrit);
            if (recordsTotal == 0) {
                return output;
            }
            output.setTotal(recordsTotal);

            Query query = QueryUtils.getQuery(this.entityInformation, input);
            if (additionalCrit != null) {
                query.addCriteria(additionalCrit);
            }

            if (preFilteringCrit != null) {
                query.addCriteria(preFilteringCrit);
            }

            Pageable pageable = QueryUtils.getPageable(input);

            Page<T> data = findAll(query, pageable, this.entityInformation.getJavaType());

            @SuppressWarnings("unchecked")
            List<R> content = converter == null ? (List<R>) data.getContent() : data.map(converter).getContent();
            output.setData(content);
            output.setFiltered(data.getTotalElements());

        } catch (Exception e) {
            output.setError(e.toString());
            output.setFiltered(0L);
            log.error("caught exception", e);
        }

        return output;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.data.mongodb.datatables.repository.DataTablesRepository#findAll(java.lang.Class,
     * org.springframework.data.jpa.datatables.mapping.QueryInput,
     * org.springframework.data.mongodb.core.aggregation.AggregationOperation[])
     */
    @Override
    public <View> QueryOutput<View> findAll(Class<View> classOfView, QueryInput input,
            AggregationOperation... operations) {
        QueryOutput<View> output = new QueryOutput<View>();
        output.setDraw(input.getDraw());

        try {
            // TODO here count() may not be accurate because Aggregation is not simply a filter
            long recordsTotal = count();
            if (recordsTotal == 0) {
                return output;
            }
            output.setTotal(recordsTotal);

            Page<View> data = findPage(this.entityInformation, classOfView, input, operations);

            output.setData(data.getContent());
            output.setFiltered(data.getTotalElements());

        } catch (Exception e) {
            output.setError(e.toString());
            output.setFiltered(0L);
            log.error("caught exception", e);
        }

        return output;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.data.mongodb.datatables.repository.DataTablesRepository#findAll(java.lang.Class,
     * org.springframework.data.jpa.datatables.mapping.QueryInput, java.util.Collection)
     */
    @Override
    public <View> QueryOutput<View> findAll(Class<View> classOfView, QueryInput input,
            Collection<? extends AggregationOperation> operations) {
        AggregationOperation[] opArray = operations.toArray(new AggregationOperation[0]);
        return findAll(classOfView, input, opArray);
    }

    private <View> Page<View> findPage(MongoEntityInformation<T, ID> entityInformation, Class<View> classOfView,
            QueryInput input, AggregationOperation... operations) {
        final Pageable pageable = QueryUtils.getPageable(input);

        final TypedAggregation<View> aggWithPage = QueryUtils.makeAggregation(classOfView, input, pageable, operations);

        final TypedAggregation<QueryCount> aggCount = QueryUtils.makeAggregationCountOnly(entityInformation, input,
                operations);
        long count = 0L;
        AggregationResults<QueryCount> countResult = mongoOperations.aggregate(aggCount,
                entityInformation.getJavaType(), QueryCount.class);

        if (countResult != null) {
            count = countResult.getUniqueMappedResult().getCount();
        }

        if (count == 0) {
            return new PageImpl<View>(Collections.<View> emptyList());
        }

        List<View> result = null;
        AggregationResults<View> aggResult = mongoOperations.aggregate(aggWithPage, entityInformation.getJavaType(),
                classOfView);
        if (aggResult != null) {
            result = aggResult.getMappedResults();
        }

        return new PageImpl<View>(result, pageable, count);
    }

}
