package com.eaphonetech.common.datatables.samples.mongo.repo;

import org.springframework.stereotype.Repository;

import com.eaphonetech.common.datatables.mongodb.repository.MongoDBQueryRepository;
import com.eaphonetech.common.datatables.samples.mongo.document.Order;

/**
 * User repository extending {@link MongoDBQueryRepository}
 *
 * @author Xiaoyu Guo
 */
@Repository
public interface OrderRepository extends MongoDBQueryRepository<Order, String> {

}
