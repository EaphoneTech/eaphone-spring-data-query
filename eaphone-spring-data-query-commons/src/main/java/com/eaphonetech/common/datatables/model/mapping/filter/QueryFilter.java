package com.eaphonetech.common.datatables.model.mapping.filter;

import lombok.Data;

@Data
public class QueryFilter {
    private String gt;
    private String gte;
    private String lt;
    private String lte;
    private String eq;
    private String ne;
    private String in;
    private String nin;
    private String regex;
    private String like;
    private Boolean exists;
    private Boolean isNull;
    private Boolean isEmpty;
}
