package com.eaphonetech.common.datatables.model.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.Min;

import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Sort;

import com.eaphonetech.common.datatables.model.mapping.filter.QueryField;

import lombok.Data;

@Data
public class QueryInput {

    /**
     * Paging first record indicator. This is the start point in the current data set (0 index based -
     * i.e. 0 is the first record).
     */
    @Min(0)
    private int offset = 0;

    /**
     * Number of records that the table can display in the current draw. It is expected that the
     * number of records returned will be equal to this number, unless the server has fewer records to
     * return. Note that this can be -1 to indicate that all records should be returned (although that
     * negates any benefits of server-side processing!)
     */
    @Min(-1)
    private int limit = 10;

    /**
     * Order parameter
     */
    private List<QueryOrder> order_by = new ArrayList<QueryOrder>();

    @Transient
    public List<Sort.Order> getOrders() {
        return this.order_by.stream().flatMap(entry -> entry.get()).collect(Collectors.toList());
    }

    /**
     * Per-column search parameter
     */
    private Map<String, QueryField> where = new HashMap<>();

    /**
     * Find a column by its name
     *
     * @param columnName the name of the column
     * @return the given Column, or <code>null</code> if not found
     */
    public QueryField getField(String columnName) {
        if (columnName == null) {
            return null;
        }
        if (this.where.containsKey(columnName)) {
            QueryField qf = this.where.get(columnName);
            qf.setField(columnName);
            return qf;
        }
        return null;
    }

    public void addField(QueryField qf) {
        if (this.where == null) {
            this.where = new HashMap<>();
        }
        this.where.put(qf.getField(), qf);
    }
}
