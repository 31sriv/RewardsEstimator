package com.retailer.rewards.service;

import com.retailer.rewards.entity.Transaction;
import com.retailer.rewards.model.Rewards;
import com.retailer.rewards.repository.CustomerRepository;
import com.retailer.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RewardsServiceImplTest {

    @InjectMocks
    private RewardsServiceImpl rewardsService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    private final Long customerId = 1002l;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
    }

    @Test
    public void testGetRewardsByCustomerId_Success() {

        Transaction tx1 = new Transaction(); // Last month (amount > 100)
        tx1.setTransactionAmount(120);
        tx1.setTransactionDate(Timestamp.valueOf(now.minusDays(10)));

        Transaction tx2 = new Transaction(); // 2nd month (amount = 70)
        tx2.setTransactionAmount(70);
        tx2.setTransactionDate(Timestamp.valueOf(now.minusDays(40)));

        Transaction tx3 = new Transaction(); // 3rd month (amount = 40)
        tx3.setTransactionAmount(40);
        tx3.setTransactionDate(Timestamp.valueOf(now.minusDays(70)));

        List<Transaction> transactions = Arrays.asList(tx1, tx2, tx3);

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(transactionRepository.findAllByCustomerIdAndTransactionDateAfter(eq(customerId), any())).thenReturn(transactions);

        Rewards rewards = rewardsService.getRewardsByCustomerId(customerId);

        assertEquals(customerId, rewards.getCustomerId());
        assertEquals(90, rewards.getLastMonthReward());       // 120 → (20 * 2) + 50 = 90
        assertEquals(20, rewards.getLastSecondMonthReward()); // 70 → 70 - 50 = 20
        assertEquals(0, rewards.getLastThirdMonthReward());   // 40 → < 50
        assertEquals(110, rewards.getTotalRewards());
    }
}