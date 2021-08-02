package com.eaphonetech.common.datatables.mongodb.repository;

import static org.springframework.data.mongodb.core.query.Query.query;

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
import com.eaphonetech.common.datatables.util.Converter;

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

	@Override
	public long count() {
		// count() -> estimatedDocumentCount()
		return this.mongoOperations.getCollection(this.entityInformation.getCollectionName()).estimatedDocumentCount();
	}

	private long count(Criteria crit) {
		return this.mongoOperations.count(query(crit), this.entityInformation.getJavaType());
	}

	private Page<T> findAll(Query q, Pageable p) {
		// count() -> estimatedDocumentCount()
		long count = q.getQueryObject().isEmpty() ? count()
				: mongoOperations.count(q, this.entityInformation.getJavaType());

		if (count == 0) {
			return new PageImpl<T>(Collections.<T>emptyList());
		}
		if (p != null) {
			if (p.getSort() == null) {
				q.limit(p.getPageSize()).skip(p.getOffset());
			} else {
				q.with(p);
			}
		}
		return new PageImpl<T>(mongoOperations.find(q, this.entityInformation.getJavaType()), p, count);
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
		return findAll(input, null, null);
	}

	@Override
	public <View> QueryOutput<View> findAll(QueryInput input, Function<T, View> converter) {
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
		return findAll(input, additionalCriteria, null);
	}

	@Override
	public QueryOutput<T> findAll(QueryInput input, Criteria additionalCriteria, Criteria preFilteringCriteria) {
		QueryOutput<T> output = new QueryOutput<>();

		try {
			long recordsTotal = preFilteringCriteria == null ? count() : count(preFilteringCriteria);
			if (recordsTotal == 0) {
				return output;
			}
			output.setTotal(recordsTotal);

			Query query = QueryUtils.getQuery(this.entityInformation, input);
			if (additionalCriteria != null) {
				query.addCriteria(additionalCriteria);
			}

			if (preFilteringCriteria != null) {
				query.addCriteria(preFilteringCriteria);
			}

			Pageable pageable = QueryUtils.getPageable(this.entityInformation, input);

			Page<T> data = findAll(query, pageable);

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

	/**
	 * @param input
	 * @param additionalCrit
	 * @param preFilteringCrit
	 * @param converter
	 * @return
	 */
	@Override
	public <View> QueryOutput<View> findAll(QueryInput input, Criteria additionalCrit, Criteria preFilteringCrit,
			Function<T, View> converter) {
		QueryOutput<T> raw = findAll(input, additionalCrit, preFilteringCrit);
		return Converter.convert(raw, converter);
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
		final Pageable pageable = QueryUtils.getPageable(entityInformation, input);

		final TypedAggregation<T> aggWithPage = QueryUtils.makeAggregation(entityInformation.getJavaType(), input,
				pageable, operations);

		final TypedAggregation<T> aggCount = QueryUtils.makeAggregationCountOnly(entityInformation, input, operations);
		long count = 0L;
		AggregationResults<QueryCount> countResult = mongoOperations.aggregate(aggCount, QueryCount.class);

		if (countResult != null) {
			count = countResult.getUniqueMappedResult().getCount();
		}

		if (count == 0) {
			return new PageImpl<View>(Collections.<View>emptyList());
		}

		List<View> result = null;
		AggregationResults<View> aggResult = mongoOperations.aggregate(aggWithPage, classOfView);
		if (aggResult != null) {
			result = aggResult.getMappedResults();
		}

		return new PageImpl<View>(result, pageable, count);
	}

}
