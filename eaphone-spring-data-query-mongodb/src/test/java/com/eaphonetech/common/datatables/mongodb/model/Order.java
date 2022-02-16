package com.eaphonetech.common.datatables.mongodb.model;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.Singular;

@Data
@Setter(AccessLevel.NONE)
@Builder
@Document(collection = "order")
public class Order {
	@Id
	private String id;

	private Date date;

	private String orderNumber;

	private boolean valid;

	private int amount;

	private double price;

	private OrderStatusEnum status;
	
	@Singular
	private List<OrderItem> items;

	@Singular
	private List<EmbeddedDeliveryHistory> deliveryHistories;

	// some pre defined documents for testing
	public static Order ORDER_0001 = Order.builder().id("id_order_0001").orderNumber("O0001").valid(true).amount(1)
			.status(OrderStatusEnum.PAID).build();

	public static List<Order> ALL = Arrays.asList(ORDER_0001);
}
