package com.eaphonetech.common.datatables.jpa;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import com.eaphonetech.common.datatables.model.mapping.CountInput;
import com.eaphonetech.common.datatables.model.mapping.QueryInput;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.FetchParent;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class SpecificationBuilder<T> extends AbstractPredicateBuilder<Specification<T>> {
	public SpecificationBuilder(QueryInput input) {
		super(input);
	}

	public SpecificationBuilder(CountInput input) {
		super(input);
	}

	@Override
	public Specification<T> build() {
		return new DataTablesSpecification<>();
	}

	private class DataTablesSpecification<S> implements Specification<S> {
		private static final long serialVersionUID = 2791974779679792727L;
		private List<Predicate> columnPredicates = new ArrayList<>();
		private List<Predicate> globalPredicates = new ArrayList<>();

		@Override
		public Predicate toPredicate(@NonNull Root<S> root, @NonNull CriteriaQuery<?> query,
				@NonNull CriteriaBuilder criteriaBuilder) {
			columnPredicates = new ArrayList<>();
			globalPredicates = new ArrayList<>();

			initPredicatesRecursively(tree, root, root, criteriaBuilder);

			boolean isCountQuery = query.getResultType() == Long.class;
			if (isCountQuery) {
				root.getFetches().clear();
			}

			return createFinalPredicate(criteriaBuilder);
		}

		private void initPredicatesRecursively(Node<Filter> node, From<S, S> from, FetchParent<S, S> fetch,
				CriteriaBuilder criteriaBuilder) {
			if (node.isLeaf()) {
				boolean hasColumnFilter = node.getData() != null;
				if (hasColumnFilter) {
					Filter columnFilter = node.getData();
					columnPredicates.add(columnFilter.createPredicate(from, criteriaBuilder, node.getName()));
				}
			}
			for (Node<Filter> child : node.getChildren()) {
				Path<Object> path = from.get(child.getName());
				// if (path instanceof AbstractPathImpl) {
				// // OneToOne and ManyToOne relationships
				// // if (((AbstractPathImpl<?>) path).getAttribute().isAssociation()) {
				// // continue;
				// // }
				// // OneToMany and ManyToMany relationships
				// // if (((AbstractPathImpl<?>) path).getAttribute().isCollection()) {
				// // continue;
				// // }
				// }
				if (child.isLeaf()) {
					initPredicatesRecursively(child, from, fetch, criteriaBuilder);
				} else {
					Join<S, S> join = from.join(child.getName(), JoinType.LEFT);
					Fetch<S, S> childFetch = fetch.fetch(child.getName(), JoinType.LEFT);
					initPredicatesRecursively(child, join, childFetch, criteriaBuilder);
				}
			}
		}

		private Predicate createFinalPredicate(CriteriaBuilder criteriaBuilder) {
			List<Predicate> allPredicates = new ArrayList<>(columnPredicates);

			if (!globalPredicates.isEmpty()) {
				allPredicates.add(criteriaBuilder.or(globalPredicates.toArray(new Predicate[0])));
			}

			return allPredicates.isEmpty() ? criteriaBuilder.conjunction()
					: criteriaBuilder.and(allPredicates.toArray(new Predicate[0]));
		}
	}

}