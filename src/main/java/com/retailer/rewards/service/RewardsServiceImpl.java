package com.retailer.rewards.service;

import com.retailer.rewards.constants.Constants;
import com.retailer.rewards.entity.Customer;
import com.retailer.rewards.entity.Transaction;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.exception.TransactionNotFoundException;
import com.retailer.rewards.model.MonthlyRewardDto;
import com.retailer.rewards.model.RewardResponseDto;
import com.retailer.rewards.model.TransactionRewardDto;
import com.retailer.rewards.repository.CustomerRepository;
import com.retailer.rewards.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RewardsServiceImpl implements RewardsService {

    private final TransactionRepository transactionRepository;

    private final CustomerRepository customerRepository;

    public RewardResponseDto getRewardsByCustomerId(Long customerId) {
        Customer customer = customerRepository.findByCustomerId(customerId).orElseThrow(() -> new CustomerNotFoundException("Customer with ID " + customerId + " not found."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime ninetyDaysAgo = now.minusDays(90).with(LocalTime.MIN);

        List<Transaction> recentTransactions = transactionRepository.findAllByCustomerIdAndTransactionDateGreaterThanEqual(customerId, Timestamp.valueOf(ninetyDaysAgo));

        if (recentTransactions.isEmpty()) {
            throw new TransactionNotFoundException("No transactions found in the past 3 months.");
        }

        // Group transactions by month (e.g "June")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM");

        Map<String, List<Transaction>> transactionsByMonth = recentTransactions.stream().sorted(Comparator.comparing(Transaction::getTransactionDate)) // sort by transaction date
                .collect(Collectors.groupingBy(tx -> tx.getTransactionDate().toLocalDateTime().format(formatter), LinkedHashMap::new,  // preserve the order of months
                        Collectors.toList()));


        List<MonthlyRewardDto> monthlyRewards = transactionsByMonth.entrySet().stream().map(entry -> buildMonthlyRewardDto(entry.getKey(), entry.getValue())).collect(Collectors.toList());

        long totalPoints = monthlyRewards.stream().mapToLong(MonthlyRewardDto::getTotalPoints).sum();

        return new RewardResponseDto(customer.getCustomerName(), monthlyRewards, totalPoints);
    }

    private MonthlyRewardDto buildMonthlyRewardDto(String monthLabel, List<Transaction> transactions) {
        List<TransactionRewardDto> transactionDtos = transactions.stream().map(tx -> {
            long points = calculateRewardPoints(tx);
            return new TransactionRewardDto(tx.getTransactionId(), (int) tx.getTransactionAmount(), points);
        }).collect(Collectors.toList());

        long monthlyPoints = transactionDtos.stream().mapToLong(TransactionRewardDto::getRewardPoints).sum();

        return new MonthlyRewardDto(monthLabel, monthlyPoints, transactionDtos);
    }

    private long calculateRewardPoints(Transaction t) {
        double amount = t.getTransactionAmount();
        if (amount > Constants.SECOND_REWARD_LIMIT) {
            return Math.round((amount - Constants.SECOND_REWARD_LIMIT) * 2 + (Constants.SECOND_REWARD_LIMIT - Constants.FIRST_REWARD_LIMIT));
        } else if (amount > Constants.FIRST_REWARD_LIMIT) {
            return Math.round(amount - Constants.FIRST_REWARD_LIMIT);
        } else {
            return 0L;
        }
    }
}