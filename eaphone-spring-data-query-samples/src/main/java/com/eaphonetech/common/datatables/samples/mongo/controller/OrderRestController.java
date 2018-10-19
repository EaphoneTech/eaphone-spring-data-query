package com.eaphonetech.common.datatables.samples.mongo.controller;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eaphonetech.common.datatables.model.mapping.QueryInput;
import com.eaphonetech.common.datatables.model.mapping.QueryOutput;
import com.eaphonetech.common.datatables.samples.mongo.document.Order;
import com.eaphonetech.common.datatables.samples.mongo.repo.OrderRepository;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller returning {@link QueryOutput}
 *
 * @author biggates2010
 */
@RestController
@Slf4j
public class OrderRestController {

    @Autowired
    private OrderRepository repo;

    @JsonView(QueryOutput.View.class)
    @GetMapping("/data/orders")
    public QueryOutput<Order> getOrders(@Valid QueryInput input, @RequestParam(required = false) Date startDate,
            @RequestParam(required = false) Date endDate) {
        boolean preFiltering = false;
        Criteria crit = Criteria.where("date");

        if (startDate != null) {
            preFiltering = true;
            crit.gte(startDate);
        }
        if (endDate != null) {
            preFiltering = true;
            crit.lt(endDate);
        }

        if (preFiltering) {
            return repo.findAll(input, crit);
        } else {
            return repo.findAll(input);
        }
    }

    @JsonView(QueryOutput.View.class)
    @PostMapping("/data/orders")
    public QueryOutput<Order> getOrdersByPost(@Valid @RequestBody QueryInput input) {
        return repo.findAll(input);
    }

    /**
     * Insert some data to Fongo
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
