package com.eaphonetech.common.datatables.model.mapping.filter;

import lombok.Data;

@Data
public class QueryFilter {
    private String _gt;
    private String _gte;
    private String _lt;
    private String _lte;
    private String _eq;
    private String _ne;
    private String _in;
    private String _nin;
    private String _regex;
    private String _like;
    private Boolean _exists;
    private Boolean _null;
    private Boolean _empty;
}
