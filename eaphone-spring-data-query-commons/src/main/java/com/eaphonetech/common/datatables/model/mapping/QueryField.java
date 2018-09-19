package com.eaphonetech.common.datatables.model.mapping;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QueryField extends Filter {

    private String field;
    private String type = ColumnType.STRING.getCode();

}
