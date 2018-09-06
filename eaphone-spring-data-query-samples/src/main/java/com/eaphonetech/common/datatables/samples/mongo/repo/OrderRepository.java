package com.eaphonetech.common.datatables.samples.mongo.repo;

import org.springframework.stereotype.Repository;

import com.eaphonetech.common.datatables.mongodb.repository.DataTablesRepository;
import com.eaphonetech.common.datatables.samples.mongo.document.Order;

/**
 * User repository extending {@link DataTablesRepository}
 *
 * @author Xiaoyu Guo
 */
@Repository
public interface OrderRepository extends DataTablesRepository<Order, String> {

}
