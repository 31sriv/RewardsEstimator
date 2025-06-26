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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RewardsServiceImpl implements RewardsService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public RewardResponseDto getRewardsByCustomerId(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer with ID " + customerId + " not found."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime ninetyDaysAgo = now.minusDays(90).with(LocalTime.MIN);


        List<Transaction> recentTransactions = transactionRepository
                .findAllByCustomerIdAndTransactionDateGreaterThanEqual(customerId, Timestamp.valueOf(ninetyDaysAgo));

        if (recentTransactions.isEmpty()) {
            throw new TransactionNotFoundException("No transactions found in the past 3 months.");
        }

        // Group transactions by month (e.g "Jun")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");

        Map<String, List<Transaction>> transactionsByMonth = recentTransactions.stream()
                .sorted(Comparator.comparing(Transaction::getTransactionDate)) // sort by transaction date
                .collect(Collectors.groupingBy(
                        tx -> tx.getTransactionDate().toLocalDateTime().format(formatter),
                        LinkedHashMap::new,  // preserve the order of months
                        Collectors.toList()
                ));

// Step 2: Build monthly reward DTOs from ordered map
        List<MonthlyRewardDto> monthlyRewards = transactionsByMonth.entrySet().stream()
                .map(entry -> buildMonthlyRewardDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        long totalPoints = monthlyRewards.stream()
                .mapToLong(MonthlyRewardDto::getTotalPoints)
                .sum();

        return new RewardResponseDto(customer.getCustomerName(), monthlyRewards, totalPoints);
    }

    private MonthlyRewardDto buildMonthlyRewardDto(String monthLabel, List<Transaction> transactions) {
        List<TransactionRewardDto> transactionDtos = transactions.stream()
                .map(tx -> {
                    long points = calculateRewardPoints(tx);
                    return new TransactionRewardDto(
                            tx.getTransactionId(),
                            (int) tx.getTransactionAmount(),
                            points
                    );
                })
                .collect(Collectors.toList());

        long monthlyPoints = transactionDtos.stream()
                .mapToLong(TransactionRewardDto::getRewardPoints)
                .sum();

        return new MonthlyRewardDto(monthLabel, monthlyPoints, transactionDtos);
    }

    private long calculateRewardPoints(Transaction t) {
        double amount = t.getTransactionAmount();
        if (amount > Constants.SECOND_REWARD_LIMIT) {
            return Math.round((amount - Constants.SECOND_REWARD_LIMIT) * 2 +
                    (Constants.SECOND_REWARD_LIMIT - Constants.FIRST_REWARD_LIMIT));
        } else if (amount > Constants.FIRST_REWARD_LIMIT) {
            return Math.round(amount - Constants.FIRST_REWARD_LIMIT);
        } else {
            return 0L;
        }
    }
}










//@Service
//public class RewardsServiceImpl implements RewardsService {
//
//    @Autowired
//    private TransactionRepository transactionRepository;
//
//    @Autowired
//    private CustomerRepository customerRepository;
//
//    public RewardResponseDto getRewardsByCustomerId(Long customerId) {
//        Customer customer = customerRepository.findById(customerId)
//                .orElseThrow(() -> new CustomerNotFoundException("Customer with ID " + customerId + " not found."));
//
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime ninetyDaysAgo = now.minusDays(90);
//
//        List<Transaction> recentTransactions = transactionRepository
//                .findAllByCustomerIdAndTransactionDateAfter(customerId, Timestamp.valueOf(ninetyDaysAgo));
//
//        if (recentTransactions.isEmpty()) {
//            throw new TransactionNotFoundException("No transactions found in the past 3 months.");
//        }
//
//        // Group transactions by month (e.g "Jun")
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");
//        Map<String, List<Transaction>> transactionsByMonth = recentTransactions.stream()
//                .collect(Collectors.groupingBy(tx -> tx.getTransactionDate().toLocalDateTime().format(formatter)));
//
//        List<MonthlyRewardDto> monthlyRewards = new ArrayList<>();
//        long totalPoints = 0;
//
//        for (Map.Entry<String, List<Transaction>> entry : transactionsByMonth.entrySet()) {
//            MonthlyRewardDto dto = buildMonthlyRewardDto(entry.getKey(), entry.getValue());
//            monthlyRewards.add(dto);
//            totalPoints += dto.getTotalPoints();
//        }
//
//        // Build final response
//        RewardResponseDto response = new RewardResponseDto();
//        response.setCustomerName(customer.getCustomerName());
//        response.setMonthlyRewards(monthlyRewards);
//        response.setTotalRewardPoints(totalPoints);
//
//        return response;
//    }
//
//    private MonthlyRewardDto buildMonthlyRewardDto(String monthLabel, List<Transaction> transactions) {
//        List<TransactionRewardDto> txDtos = new ArrayList<>();
//        long monthlyPoints = 0;
//
//        for (Transaction tx : transactions) {
//            long points = calculateRewardPoints(tx);
//            monthlyPoints += points;
//
//            TransactionRewardDto txDto = new TransactionRewardDto();
//            txDto.setTransactionId(tx.getTransactionId());
//            txDto.setAmount((int) tx.getTransactionAmount());
//            txDto.setRewardPoints(points);
//            txDtos.add(txDto);
//        }
//
//        MonthlyRewardDto dto = new MonthlyRewardDto();
//        dto.setMonth(monthLabel);
//        dto.setTotalPoints(monthlyPoints);
//        dto.setTransactions(txDtos);
//        return dto;
//    }
//
//    private long calculateRewardPoints(Transaction t) {
//        double amount = t.getTransactionAmount();
//        if (amount > Constants.SECOND_REWARD_LIMIT) {
//            return Math.round((amount - Constants.SECOND_REWARD_LIMIT) * 2 +
//                    (Constants.SECOND_REWARD_LIMIT - Constants.FIRST_REWARD_LIMIT));
//        } else if (amount > Constants.FIRST_REWARD_LIMIT) {
//            return Math.round(amount - Constants.FIRST_REWARD_LIMIT);
//        } else {
//            return 0L;
//        }
//    }
//}


//@Service
//public class RewardsServiceImpl implements RewardsService{
//
//    @Autowired
//    private CustomerRepository customerRepository;
//
//    @Autowired
//    private TransactionRepository transactionRepository;
//
//    public RewardResponseDto getRewardsByCustomerId(Long customerId) {
//        Customer customer = customerRepository.findById(customerId)
//                .orElseThrow(() -> new RuntimeException("Customer not found"));
//
//        // Only transactions from the last 3 months
//        LocalDateTime now = LocalDateTime.now(); // use fixed current date to match your data
//        LocalDateTime threeMonthsAgo = now.minusMonths(3).withDayOfMonth(1);
//
//        List<Transaction> transactions = transactionRepository
//                .findAllByCustomerIdAndTransactionDateAfter(customerId, Timestamp.valueOf(threeMonthsAgo));
//
//        Map<YearMonth, List<Transaction>> transactionsByMonth = transactions.stream()
//                .collect(Collectors.groupingBy(tx -> YearMonth.from(tx.getTransactionDate().toLocalDateTime())));
//
//        List<MonthlyRewardDto> monthlyRewards = new ArrayList<>();
//        int overallTotalPoints = 0;
//
//        for (Map.Entry<YearMonth, List<Transaction>> entry : transactionsByMonth.entrySet()) {
//            YearMonth month = entry.getKey();
//            List<Transaction> txList = entry.getValue();
//
//            List<TransactionRewardDto> rewardTransactions = new ArrayList<>();
//            int monthlyTotal = 0;
//
//            for (Transaction tx : txList) {
//                int points = calculateRewardPoints(tx.getTransactionAmount());
//                rewardTransactions.add(new TransactionRewardDto(tx.getTransactionId(), tx.getTransactionAmount(), points));
//                monthlyTotal += points;
//            }
//
//            // Format: "Jun"
//            String formattedMonth = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
//
//            monthlyRewards.add(new MonthlyRewardDto(formattedMonth, monthlyTotal, rewardTransactions));
//            overallTotalPoints += monthlyTotal;
//        }
//
//        // Sort months descending
//        monthlyRewards.sort((m1, m2) -> YearMonth.parse(m2.getMonth(), DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH))
//                .compareTo(YearMonth.parse(m1.getMonth(), DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH))));
//
//        return new RewardResponseDto(customer.getCustomerName(), monthlyRewards, overallTotalPoints);
//    }
//
//    private int calculateRewardPoints(int amount) {
//        if (amount <= 50) return 0;
//        if (amount <= 100) return amount - 50;
//        return (amount - 100) * 2 + 50;
//    }
//}































//
//
//    @Autowired
//    private TransactionRepository transactionRepository;
//
//    @Autowired
//    private CustomerRepository customerRepository;
//
//    public Rewards getRewardsByCustomerId1(Long customerId) {
//        if (!customerRepository.existsById(customerId)) {
//            throw new CustomerNotFoundException("Customer with ID " + customerId + " not found.");
//        }
//
//        LocalDateTime currentDT = LocalDateTime.now();
//        LocalDateTime thirdMonthStart = currentDT.minusDays(90);
//
//        // Fetch all transactions from last 90 days
//        List<Transaction> recentTransactions = transactionRepository
//                .findAllByCustomerIdAndTransactionDateAfter(customerId, Timestamp.valueOf(thirdMonthStart));
//
//        if (recentTransactions.isEmpty()) {
//            throw new TransactionNotFoundException("No transactions found in the past 3 months.");
//        }
//
//        // Filter by each month's time window
//        long lastMonthPoints = getRewardsPerMonth(
//                recentTransactions.stream()
//                        .filter(tx -> tx.getTransactionDate().toLocalDateTime().isAfter(currentDT.minusDays(30)))
//                        .collect(Collectors.toList()));
//
//        long secondMonthPoints = getRewardsPerMonth(
//                recentTransactions.stream()
//                        .filter(tx -> {
//                            LocalDateTime date = tx.getTransactionDate().toLocalDateTime();
//                            return date.isAfter(currentDT.minusDays(60)) && date.isBefore(currentDT.minusDays(30));
//                        }).collect(Collectors.toList()));
//
//        long thirdMonthPoints = getRewardsPerMonth(
//                recentTransactions.stream()
//                        .filter(tx -> {
//                            LocalDateTime date = tx.getTransactionDate().toLocalDateTime();
//                            return date.isAfter(currentDT.minusDays(90)) && date.isBefore(currentDT.minusDays(60));
//                        }).collect(Collectors.toList()));
//
//        Rewards rewards = new Rewards();
//        rewards.setCustomerId(customerId);
//        rewards.setLastMonthReward(lastMonthPoints);
//        rewards.setLastSecondMonthReward(secondMonthPoints);
//        rewards.setLastThirdMonthReward(thirdMonthPoints);
//        rewards.setTotalRewards(lastMonthPoints + secondMonthPoints + thirdMonthPoints);
//
//        return rewards;
//    }
//
//    private Long getRewardsPerMonth(List<Transaction> transactions) {
//        return transactions.stream().map(tx -> calculateRewardPoints(tx))
//                .collect(Collectors.summingLong(r -> r.longValue()));
//    }

