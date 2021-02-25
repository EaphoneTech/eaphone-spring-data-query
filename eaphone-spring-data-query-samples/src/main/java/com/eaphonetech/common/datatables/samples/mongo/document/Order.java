package com.eaphonetech.common.datatables.samples.mongo.document;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.eaphonetech.common.datatables.model.mapping.QueryOutput;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.Data;

/**
 * Sample model
 *
 * @author Damien Arrachequesne
 */
@Data
@Document(collection = "order")
public class Order {

	@Id
	@JsonView(QueryOutput.View.class)
	private String id;

	@JsonFormat(pattern = "yyyy-MM-dd")
	@JsonView(QueryOutput.View.class)
	private Date date;

	@JsonView(QueryOutput.View.class)
	private String orderNumber;

	@JsonView(QueryOutput.View.class)
	private boolean isValid;

	@JsonView(QueryOutput.View.class)
	private int amount;

	@JsonView(QueryOutput.View.class)
	private double price;

	@JsonView(QueryOutput.View.class)
	private Set<OrderItem> items;

	@Transient
	public static Order random() {
		Order o = new Order();
		Random r = new Random();

		Calendar c = Calendar.getInstance();
		c.set(2005 + r.nextInt(10), r.nextInt(12), r.nextInt(28), r.nextInt(24), r.nextInt(59), r.nextInt(59));
		o.date = c.getTime();

		o.orderNumber = String.format("O%05d", r.nextInt(99999));

		o.isValid = r.nextBoolean();

		o.amount = r.nextInt(25);

		o.price = Math.round(100.0 * 100.0 * r.nextDouble()) / 100.0;

		Set<OrderItem> items = new HashSet<>();
		for (int i = 0; i < r.nextInt(8); i++) {
			int amount = r.nextInt(25);
			double price = Math.round(100.0 * 100.0 * r.nextDouble()) / 100.0;

			OrderItem item = new OrderItem();
			item.setId(new ObjectId().toHexString());
			item.setName(o.getOrderNumber() + "_" + i);
			item.setAmount(amount);
			item.setPrice(price);
			item.setDate(o.getDate());
			item.setValid(o.isValid());
			items.add(item);

			o.setAmount(o.getAmount() + amount);
			o.setPrice(o.getPrice() + price);
		}
		o.setItems(items);

		return o;
	}
}
