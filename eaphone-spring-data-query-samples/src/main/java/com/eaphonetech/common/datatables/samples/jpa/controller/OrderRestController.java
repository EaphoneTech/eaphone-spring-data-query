package com.eaphonetech.common.datatables.samples.jpa.controller;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.eaphonetech.common.datatables.model.mapping.CountInput;
import com.eaphonetech.common.datatables.model.mapping.CountOutput;
import com.eaphonetech.common.datatables.model.mapping.QueryInput;
import com.eaphonetech.common.datatables.model.mapping.QueryOutput;
import com.eaphonetech.common.datatables.samples.jpa.entities.Order;
import com.eaphonetech.common.datatables.samples.jpa.repo.OrderRepository;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class OrderRestController {

	@Autowired
	private OrderRepository repo;

	@JsonView(QueryOutput.View.class)
	@PostMapping("/data/orders/search")
	public QueryOutput<Order> getOrdersByPost(@Valid @RequestBody QueryInput input) {
		return repo.findAll(input);
	}

	@PostMapping("/data/orders/count")
	public CountOutput count(@Valid @RequestBody CountInput input) {
		return repo.count(input);
	}

	/**
	 * Insert some data to h2
	 */
	@PostConstruct
	public void insertSampleData() {
		log.debug("initializing default data...");

		// some random orders
		for (int i = 0; i < 200; i++) {
			Order o = Order.random();

			repo.save(o);
		}

		// some orders with specific values
		Order o = Order.random();
		o.setOrderNumber("O10001");
		repo.save(o);

		o = Order.random();
		o.setOrderNumber("O10002");
		repo.save(o);

		log.debug("default data successfully initialized.");
	}
}
