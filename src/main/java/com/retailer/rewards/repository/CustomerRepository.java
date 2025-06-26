package com.retailer.rewards.repository;

import com.retailer.rewards.entity.Customer;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
    public Optional<Customer> findByCustomerId(Long customerId);
}
