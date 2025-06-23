package com.retailer.rewards.repository;

import com.retailer.rewards.entity.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    public List<Transaction> findAllByCustomerId(Long customerId);

    List<Transaction> findAllByCustomerIdAndTransactionDateAfter(Long customerId, Timestamp afterDate);
}
