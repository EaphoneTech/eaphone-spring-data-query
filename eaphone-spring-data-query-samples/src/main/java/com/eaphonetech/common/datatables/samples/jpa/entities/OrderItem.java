package com.eaphonetech.common.datatables.samples.jpa.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.eaphonetech.common.datatables.model.mapping.QueryOutput;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.Data;

@Data
@Entity
@Table(name = "t_order_item")
public class OrderItem {

	@Id
	@GeneratedValue
	@JsonView(QueryOutput.View.class)
	private Integer id;

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
	private boolean isValid;

}
