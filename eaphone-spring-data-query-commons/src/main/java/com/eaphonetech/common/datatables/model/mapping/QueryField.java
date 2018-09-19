package com.eaphonetech.common.datatables.model.mapping;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QueryField extends Filter {

    /**
     * Column's data source
     * 
     * @see http://datatables.net/reference/option/columns.data
     */
    @NotBlank
    private String field;

    private String type = ColumnType.STRING.getCode();

}
