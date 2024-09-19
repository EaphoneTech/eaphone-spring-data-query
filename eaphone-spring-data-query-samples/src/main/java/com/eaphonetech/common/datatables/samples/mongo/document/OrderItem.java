package com.eaphonetech.common.datatables.samples.mongo.document;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Field;

import com.eaphonetech.common.datatables.model.mapping.QueryOutput;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.Data;

@Data
public class OrderItem {

	@Field("id")
	@JsonView(QueryOutput.View.class)
	private String id;

	@JsonView(QueryOutput.View.class)
	private String name;

	@JsonView(QueryOutput.View.class)
	private int amount;

	@JsonView(QueryOutput.View.class)
	private double price;

	@JsonFormat(pattern = "yyyy-MM-dd")
	@JsonView(QueryOutput.View.class)
	private Date date;

	@JsonView(QueryOutput.View.class)
	private Boolean isValid;

}
