package com.eaphonetech.common.datatables.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import com.eaphonetech.common.datatables.model.mapping.ColumnType;
import com.eaphonetech.common.datatables.model.mapping.QueryInput;
import com.eaphonetech.common.datatables.model.mapping.filter.QueryField;
import com.eaphonetech.common.datatables.model.mapping.filter.QueryFilter;

abstract class AbstractPredicateBuilder<T> {
    protected final QueryInput input;
    final Node<Filter> tree;

    AbstractPredicateBuilder(QueryInput input) {
        this.input = input;
        tree = new Node<>(null);
        initTree(input);
    }

    private void initTree(QueryInput input) {
        for (Map.Entry<String, QueryField> entry : input.getWhere().entrySet()) {
            final String fieldName = entry.getKey();
            final QueryField field = entry.getValue();
            addChild(tree, 0, fieldName.split("\\."), ColumnType.parse(field.getType()), field);
        }
    }

    private void addChild(Node<Filter> parent, int index, String[] names, ColumnType type, QueryFilter filter) {
        boolean isLast = index + 1 == names.length;
        if (isLast) {
            Node<Filter> child = new Node<Filter>(names[index], new ColumnFilter(type, filter));
            parent.addChild(child);
        } else {
            Node<Filter> child = parent.getOrCreateChild(names[index]);
            addChild(child, index + 1, names, type, filter);
        }
    }

    /**
     * Creates a 'LIMIT .. OFFSET .. ORDER BY ..' clause for the given {@link QueryInput}.
     *
     * @return a {@link Pageable}, must not be {@literal null}.
     */
    public Pageable createPageable() {
        List<Sort.Order> orders = new ArrayList<>();
        for (String sortColumn : input.getOrder_by().keySet()) {
            Sort.Direction sortDirection = Sort.Direction.fromString(input.getOrder_by().get(sortColumn).name());
            orders.add(new Sort.Order(sortDirection, sortColumn));
        }
        Sort sort = orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);

        if (input.getLimit() == -1) {
            input.setOffset(0);
            input.setLimit(Integer.MAX_VALUE);
        }
        return new DataTablesPageRequest(input.getOffset(), input.getLimit(), sort);
    }

    public abstract T build();

    private class DataTablesPageRequest implements Pageable {
        private final int offset;
        private final int limit;
        private final Sort sort;

        DataTablesPageRequest(int offset, int limit, Sort sort) {
            this.offset = offset;
            this.limit = limit;
            this.sort = sort;
        }

        @Override
        public long getOffset() {
            return offset;
        }

        @Override
        public int getPageSize() {
            return limit;
        }

        @Override
        @NonNull
        public Sort getSort() {
            return sort;
        }

        @Override
        @NonNull
        public Pageable next() {
            throw new UnsupportedOperationException();
        }

        @Override
        @NonNull
        public Pageable previousOrFirst() {
            throw new UnsupportedOperationException();
        }

        @Override
        @NonNull
        public Pageable first() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasPrevious() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getPageNumber() {
            throw new UnsupportedOperationException();
        }
    }

}