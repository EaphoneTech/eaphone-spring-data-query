package com.eaphonetech.common.datatables.model.mapping.filter;

import java.util.ArrayList;

public class QueryFilterBuilder extends QueryFilter {

	public QueryFilter build() {
		return this;
	}

	public QueryFilterBuilder gte(Object o) {
		this.set_gte(o);
		return this;
	}

	public QueryFilterBuilder lt(Object o) {
		this.set_lt(o);
		return this;
	}

	public QueryFilterBuilder lte(Object o) {
		this.set_lte(o);
		return this;
	}

	public QueryFilterBuilder eq(Object o) {
		this.set_eq(o);
		return this;
	}

	public QueryFilterBuilder ne(Object o) {
		this.set_ne(o);
		return this;
	}

	public QueryFilterBuilder in(Object o) {
		if (this.get_in() == null) {
			this.set_in(new ArrayList<>());
		}
		this.get_in().add(o);
		return this;
	}

	public QueryFilterBuilder nin(Object o) {
		if (this.get_nin() == null) {
			this.set_nin(new ArrayList<>());
		}
		this.get_nin().add(o);
		return this;
	}

	public QueryFilterBuilder regex(String regex) {
		this.set_regex(regex);
		return this;
	}

	public QueryFilterBuilder like(String like) {
		this.set_like(like);
		return this;
	}

	public QueryFilterBuilder exists(boolean exists) {
		this.set_exists(exists);
		return this;
	}

	public QueryFilterBuilder isNull(boolean isNull) {
		this.set_null(isNull);
		return this;
	}

	public QueryFilterBuilder empty(boolean isEmpty) {
		this.set_empty(isEmpty);
		return this;
	}
}
