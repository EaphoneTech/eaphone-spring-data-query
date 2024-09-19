package com.eaphonetech.common.datatables.model.mapping.filter;

import java.util.List;

import lombok.Data;

@Data
public class QueryFilter {
    private Object _gt;
    private Object _gte;
    private Object _lt;
    private Object _lte;
    private Object _eq;
    private Object _ne;
    private List<Object> _in;
    private List<Object> _nin;
    private String _regex;
    private String _like;
    private Boolean _exists;
    private Boolean _null;
    private Boolean _empty;
    private Boolean _isvoid;
}
