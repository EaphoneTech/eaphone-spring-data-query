package com.eaphonetech.common.datatables.model.mapping.filter;

import com.eaphonetech.common.datatables.model.mapping.ColumnType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QueryField extends QueryFilter {

    private String field;
    private String type = ColumnType.STRING.getCode();

}
