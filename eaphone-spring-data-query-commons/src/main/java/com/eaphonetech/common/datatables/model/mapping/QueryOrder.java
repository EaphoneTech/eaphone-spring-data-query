package com.eaphonetech.common.datatables.model.mapping;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QueryOrder {

    /**
     * Column to which ordering should be applied. This is an index reference to the columns array of
     * information that is also submitted to the server.
     */
    @NotNull
    @Min(1)
    private String field;

    /**
     * Ordering direction for this column. It will be asc or desc to indicate ascending ordering or
     * descending ordering, respectively.
     */
    @NotNull
    @Pattern(regexp = "(desc|asc)")
    private String dir;

}
