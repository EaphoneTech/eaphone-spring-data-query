package com.eaphonetech.common.datatables.samples.jpa.repo;

import org.springframework.stereotype.Repository;

import com.eaphonetech.common.datatables.jpa.repository.JpaQueryRepository;
import com.eaphonetech.common.datatables.samples.jpa.entities.Order;

@Repository
public interface OrderRepository extends JpaQueryRepository<Order, Integer> {

}
