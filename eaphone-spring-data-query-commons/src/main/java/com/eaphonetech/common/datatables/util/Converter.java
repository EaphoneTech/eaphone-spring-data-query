package com.eaphonetech.common.datatables.util;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.eaphonetech.common.datatables.model.mapping.QueryOutput;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Converter {

    public static <SRC, TARGET> QueryOutput<TARGET> convert(QueryOutput<SRC> src, Function<SRC, TARGET> converter) {
        Objects.requireNonNull(converter);
        QueryOutput<TARGET> output = new QueryOutput<>();

        output.setDraw(src.getDraw());
        List<TARGET> content = null;
        if (src.getData() == null) {
            content = Collections.emptyList();
        } else {
            content = src.getData().stream().map(converter).collect(Collectors.toList());
        }
        output.setData(content);
        output.setError(src.getError());
        output.setFiltered(src.getFiltered());
        output.setTotal(src.getTotal());

        return output;
    }
}
