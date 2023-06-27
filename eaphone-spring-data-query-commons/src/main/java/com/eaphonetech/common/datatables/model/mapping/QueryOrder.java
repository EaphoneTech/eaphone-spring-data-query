package com.eaphonetech.common.datatables.model.mapping;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
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
	private OrderValue dir;

	@JsonAnySetter
	private Map<String, OrderValue> values = new LinkedHashMap<>();

	public static enum OrderValue {
		@JsonEnumDefaultValue
		asc(Direction.ASC), desc(Direction.DESC);

		@Getter
		private Direction direction;

		OrderValue(Direction dir) {
			this.direction = dir;
		}
	}

	@Transient
	Stream<Sort.Order> get() {
		return this.values.entrySet().stream()
				.map(entry -> new Sort.Order(entry.getValue().getDirection(), entry.getKey()));
	}
}