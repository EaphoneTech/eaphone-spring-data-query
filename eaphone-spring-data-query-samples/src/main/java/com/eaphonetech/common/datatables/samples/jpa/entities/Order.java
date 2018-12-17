package com.eaphonetech.common.datatables.samples.jpa.entities;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.data.annotation.Transient;

import com.eaphonetech.common.datatables.model.mapping.QueryOutput;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.Data;

@Data
@Entity
@Table(name = "t_order")
public class Order {

	@Id
	@GeneratedValue
	@JsonView(QueryOutput.View.class)
	private Integer id;

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
	@OneToOne(targetEntity = OrderUser.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
	private OrderUser user;

	@JsonView(QueryOutput.View.class)
	@OneToMany(targetEntity = OrderItem.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "order_id", referencedColumnName = "id")
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
		o.setAmount(0);
		o.setPrice(0D);

		OrderUser user = new OrderUser();
		user.setName("张三");
		user.setBirthday(o.getDate());
		user.setAge(r.nextInt(30));
		user.setBlood("ABO".charAt(r.nextInt(3)));
		user.setValid(r.nextBoolean());
		o.setUser(user);

		Set<OrderItem> items = new HashSet<>();
		for (int i = 0; i < r.nextInt(8); i++) {
			int amount = r.nextInt(25);
			double price = Math.round(100.0 * 100.0 * r.nextDouble()) / 100.0;

			OrderItem item = new OrderItem();
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
