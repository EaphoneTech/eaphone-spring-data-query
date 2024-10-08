package com.eaphonetech.common.datatables.samples.jpa.entities;

import java.util.Date;

import com.eaphonetech.common.datatables.model.mapping.QueryOutput;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "t_order_user")
public class OrderUser {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@JsonView(QueryOutput.View.class)
	private Integer id;

	@JsonView(QueryOutput.View.class)
	private String name;

	@JsonFormat(pattern = "yyyy-MM-dd")
	@JsonView(QueryOutput.View.class)
	private Date birthday;

	@JsonView(QueryOutput.View.class)
	private int age;

	@JsonView(QueryOutput.View.class)
	private char blood;

	@JsonView(QueryOutput.View.class)
	private boolean isValid;

}
