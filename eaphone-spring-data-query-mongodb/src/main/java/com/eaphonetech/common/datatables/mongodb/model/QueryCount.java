package com.eaphonetech.common.datatables.mongodb.model;

import org.springframework.data.mongodb.core.mapping.Field;

public class QueryCount {
    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Field("_count")
    private long count;
}
