package com.retailer.rewards.repository;

import com.retailer.rewards.entity.Customer;
import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
    public Customer findByCustomerId(Long customerId);
}
