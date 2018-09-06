package com.eaphonetech.common.datatables.jpa;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import com.eaphonetech.common.datatables.model.mapping.Column;
import com.eaphonetech.common.datatables.model.mapping.ColumnType;
import com.eaphonetech.common.datatables.model.mapping.DataTablesInput;
import com.eaphonetech.common.datatables.model.mapping.Search;

abstract class AbstractPredicateBuilder<T> {
    protected final DataTablesInput input;
    final boolean hasGlobalFilter;
    final Node<Filter> tree;

    AbstractPredicateBuilder(DataTablesInput input) {
        this.input = input;
        this.hasGlobalFilter = input.getSearch() != null && StringUtils.hasText(input.getSearch().getValue());
        if (this.hasGlobalFilter) {
            tree = new Node<>(null, new GlobalFilter(input.getSearch().getValue()));
        } else {
            tree = new Node<>(null);
        }
        initTree(input);
    }

    private void initTree(DataTablesInput input) {
        for (Column column : input.getColumns()) {
            if (column.isSearchable()) {
                // datatables impl.
                addChild(tree, 0, column.getData().split("\\."), column.getSearch());
            }
            if (column.getFilter() != null) {
                addChild(tree, 0, column.getData().split("\\."), ColumnType.parse(column.getType()),
                        column.getFilter());
            }
        }
    }

    private void addChild(Node<Filter> parent, int index, String[] names, ColumnType type,
            com.eaphonetech.common.datatables.model.mapping.Filter filter) {
        boolean isLast = index + 1 == names.length;
        if (isLast) {
            Node<Filter> child = new Node<Filter>(names[index], new ColumnFilter(type, filter));
            parent.addChild(child);
        } else {
            Node<Filter> child = parent.getOrCreateChild(names[index]);
            addChild(child, index + 1, names, type, filter);
        }
    }

    private void addChild(Node<Filter> parent, int index, String[] names, Search search) {
        boolean isLast = index + 1 == names.length;
        if (isLast) {
            boolean hasColumnFilter = search != null && StringUtils.hasText(search.getValue());
            parent.addChild(
                    new Node<>(names[index], hasColumnFilter ? new ColumnSearchFilter(search.getValue()) : null));
        } else {
            Node<Filter> child = parent.getOrCreateChild(names[index]);
            addChild(child, index + 1, names, search);
        }
    }

    /**
     * Creates a 'LIMIT .. OFFSET .. ORDER BY ..' clause for the given {@link DataTablesInput}.
     *
     * @return a {@link Pageable}, must not be {@literal null}.
     */
    public Pageable createPageable() {
        List<Sort.Order> orders = new ArrayList<>();
        for (com.eaphonetech.common.datatables.model.mapping.Order order : input.getOrder()) {
            Column column = input.getColumns().get(order.getColumn());
            if (column.isOrderable()) {
                String sortColumn = column.getData();
                Sort.Direction sortDirection = Sort.Direction.fromString(order.getDir());
                orders.add(new Sort.Order(sortDirection, sortColumn));
            }
        }
        Sort sort = orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);

        if (input.getLength() == -1) {
            input.setStart(0);
            input.setLength(Integer.MAX_VALUE);
        }
        return new DataTablesPageRequest(input.getStart(), input.getLength(), sort);
    }

    public abstract T build();

    private class DataTablesPageRequest implements Pageable {
        private final int offset;
        private final int pageSize;
        private final Sort sort;

        DataTablesPageRequest(int offset, int pageSize, Sort sort) {
            this.offset = offset;
            this.pageSize = pageSize;
            this.sort = sort;
        }

        @Override
        public long getOffset() {
            return offset;
        }

        @Override
        public int getPageSize() {
            return pageSize;
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