package com.retailer.rewards.service;

import com.retailer.rewards.constants.Constants;
import com.retailer.rewards.entity.Customer;
import com.retailer.rewards.entity.Transaction;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.exception.TransactionNotFoundException;
import com.retailer.rewards.model.Rewards;
import com.retailer.rewards.repository.CustomerRepository;
import com.retailer.rewards.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RewardsServiceImpl implements RewardsService {


    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public Rewards getRewardsByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer with ID " + customerId + " not found.");
        }

        LocalDateTime currentDT = LocalDateTime.now();
        LocalDateTime thirdMonthStart = currentDT.minusDays(90);

        // Fetch all transactions from last 90 days
        List<Transaction> recentTransactions = transactionRepository
                .findAllByCustomerIdAndTransactionDateAfter(customerId, Timestamp.valueOf(thirdMonthStart));

        if (recentTransactions.isEmpty()) {
            throw new TransactionNotFoundException("No transactions found in the past 3 months.");
        }

        // Filter by each month's time window
        long lastMonthPoints = getRewardsPerMonth(
                recentTransactions.stream()
                        .filter(tx -> tx.getTransactionDate().toLocalDateTime().isAfter(currentDT.minusDays(30)))
                        .collect(Collectors.toList()));

        long secondMonthPoints = getRewardsPerMonth(
                recentTransactions.stream()
                        .filter(tx -> {
                            LocalDateTime date = tx.getTransactionDate().toLocalDateTime();
                            return date.isAfter(currentDT.minusDays(60)) && date.isBefore(currentDT.minusDays(30));
                        }).collect(Collectors.toList()));

        long thirdMonthPoints = getRewardsPerMonth(
                recentTransactions.stream()
                        .filter(txn -> {
                            LocalDateTime date = txn.getTransactionDate().toLocalDateTime();
                            return date.isAfter(currentDT.minusDays(90)) && date.isBefore(currentDT.minusDays(60));
                        }).collect(Collectors.toList()));

        Rewards rewards = new Rewards();
        rewards.setCustomerId(customerId);
        rewards.setLastMonthReward(lastMonthPoints);
        rewards.setLastSecondMonthReward(secondMonthPoints);
        rewards.setLastThirdMonthReward(thirdMonthPoints);
        rewards.setTotalRewards(lastMonthPoints + secondMonthPoints + thirdMonthPoints);

        return rewards;
    }

    private Long getRewardsPerMonth(List<Transaction> transactions) {
        return transactions.stream().map(tx -> calculateRewardPoints(tx))
                .collect(Collectors.summingLong(r -> r.longValue()));
    }

    private long calculateRewardPoints(Transaction t) {
        double amount = t.getTransactionAmount();
        if (amount > Constants.secondRewardLimit) {
            return Math.round((amount - Constants.secondRewardLimit) * 2 +
                    (Constants.secondRewardLimit - Constants.firstRewardLimit));
        } else if (amount > Constants.firstRewardLimit) {
            return Math.round(amount - Constants.firstRewardLimit);
        } else {
            return 0L;
        }
    }
}
