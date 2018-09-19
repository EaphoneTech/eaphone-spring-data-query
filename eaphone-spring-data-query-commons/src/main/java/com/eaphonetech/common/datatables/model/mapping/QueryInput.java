package com.eaphonetech.common.datatables.model.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Min;

import com.eaphonetech.common.datatables.model.mapping.filter.QueryField;

import lombok.Data;

@Data
public class QueryInput {

    /**
     * Draw counter. This is used by DataTables to ensure that the Ajax returns from server-side
     * processing requests are drawn in sequence by DataTables (Ajax requests are asynchronous and
     * thus can return out of sequence). This is used as part of the draw return parameter (see
     * below).
     */
    @Min(0)
    private int draw = 1;

    /**
     * Paging first record indicator. This is the start point in the current data set (0 index based -
     * i.e. 0 is the first record).
     */
    @Min(0)
    private int start = 0;

    /**
     * Number of records that the table can display in the current draw. It is expected that the
     * number of records returned will be equal to this number, unless the server has fewer records to
     * return. Note that this can be -1 to indicate that all records should be returned (although that
     * negates any benefits of server-side processing!)
     */
    @Min(-1)
    private int length = 10;

    /**
     * Global search parameter.
     */
    private Search search = null;

    /**
     * Order parameter
     */
    private List<QueryOrder> orders = new ArrayList<QueryOrder>();

    /**
     * Per-column search parameter
     */
    private Map<String, QueryField> filters = new HashMap<>();

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
        if (this.filters.containsKey(columnName)) {
            QueryField qf = this.filters.get(columnName);
            qf.setField(columnName);
            return qf;
        }
        return null;
    }

    public void addField(QueryField qf) {
        if (this.filters == null) {
            this.filters = new HashMap<>();
        }
        this.filters.put(qf.getField(), qf);
    }
}
