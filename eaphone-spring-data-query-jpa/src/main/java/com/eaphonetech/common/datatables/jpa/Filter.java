package com.eaphonetech.common.datatables.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import com.querydsl.core.types.dsl.PathBuilder;

interface Filter {

    Predicate createPredicate(From<?, ?> from, CriteriaBuilder criteriaBuilder, String attributeName);

    com.querydsl.core.types.Predicate createPredicate(PathBuilder<?> pathBuilder, String attributeName);
}