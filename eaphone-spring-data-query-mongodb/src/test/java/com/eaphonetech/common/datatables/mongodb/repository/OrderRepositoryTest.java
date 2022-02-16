package com.eaphonetech.common.datatables.mongodb.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.eaphonetech.common.datatables.model.mapping.QueryInput;
import com.eaphonetech.common.datatables.model.mapping.QueryOutput;
import com.eaphonetech.common.datatables.model.mapping.filter.QueryFilter;
import com.eaphonetech.common.datatables.model.mapping.filter.QueryFilterBuilder;
import com.eaphonetech.common.datatables.mongodb.config.Config;
import com.eaphonetech.common.datatables.mongodb.model.Order;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
public class OrderRepositoryTest {

	@Autowired
	private OrderRepository orderRepo;

	private QueryInput where(String field, QueryFilter filter) {
		QueryInput input = getBasicInput();
		input.getWhere().put(field, filter);
		return input;
	}

	@Before
	public void init() {
		orderRepo.deleteAll();
		orderRepo.saveAll(Order.ALL);
	}

	private static QueryInput getBasicInput() {
		QueryInput input = new QueryInput();
		return input;
	}

	@Test
	public void searchByIdTest() {
		QueryInput input = where("id", new QueryFilterBuilder().eq(Order.ORDER_0001.getId()).build());
		QueryOutput<Order> output = orderRepo.findAll(input);
		assertThat(output.getData()).containsOnly(Order.ORDER_0001);
	}
}
