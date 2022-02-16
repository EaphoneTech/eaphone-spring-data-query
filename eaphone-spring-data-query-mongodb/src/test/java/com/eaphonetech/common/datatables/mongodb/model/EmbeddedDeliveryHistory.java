package com.eaphonetech.common.datatables.mongodb.model;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Data
@Builder
@Setter(AccessLevel.NONE)
public class EmbeddedDeliveryHistory {
	@Field("id")
	private String id;
	
	private Date date;
	
	private String text;
}
