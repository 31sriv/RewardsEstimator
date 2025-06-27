package com.retailer.rewards.service;

import com.retailer.rewards.entity.Customer;
import com.retailer.rewards.entity.Transaction;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.model.RewardResponseDto;
import com.retailer.rewards.repository.CustomerRepository;
import com.retailer.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RewardsServiceImplTests {
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private RewardsServiceImpl rewardsService;

    @Test
    public void testGetRewardsByCustomerId_Success() {

        // Tests reward calculation for a customer based on recent transactions

        Customer customer = new Customer(1002L, "Pawan Sehgal");
        when(customerRepository.findByCustomerId(1002L)).thenReturn(Optional.of(customer));

        List<Transaction> transactions = Arrays.asList(new Transaction(10006L, 1002L, Timestamp.valueOf(LocalDateTime.now().minusDays(2)), 120), new Transaction(10007L, 1002L, Timestamp.valueOf(LocalDateTime.now().minusDays(30)), 120));

        when(transactionRepository.findAllByCustomerIdAndTransactionDateGreaterThanEqual(eq(1002L), any(Timestamp.class))).thenReturn(transactions);

        RewardResponseDto response = rewardsService.getRewardsByCustomerId(1002L);

        assertEquals("Pawan Sehgal", response.getCustomerName());
        int totalTransactions = response.getMonthlyRewards().stream().mapToInt(month -> month.getTransactions().size()).sum();
        assertEquals(2, totalTransactions);
    }

    @Test
    public void testEdgeCase_Exactly90DaysAgo() {

        // Tests if the 90-day cutoff edge case is correctly included in reward Calculation

        Customer customer = new Customer(1001L, "Kriti Sen");

        when(customerRepository.findByCustomerId(1001L)).thenReturn(Optional.of(customer));
        Transaction tx = new Transaction(10006L, 1001L, Timestamp.valueOf(LocalDateTime.now().minusDays(90)), 120);

        when(transactionRepository.findAllByCustomerIdAndTransactionDateGreaterThanEqual(anyLong(), any())).thenReturn(List.of(tx));

        RewardResponseDto response = rewardsService.getRewardsByCustomerId(1001L);
        assertEquals("Kriti Sen", response.getCustomerName());
        assertEquals(90, response.getTotalRewardPoints());
    }

    @Test
    void testVeryLargeTransactionAmount() {

        // Tests reward calculation for a customer with a very large transaction amount

        Customer customer = new Customer(1007L, "Kriti Sen");

        Transaction tx = new Transaction(10050L, 1007L, Timestamp.valueOf(LocalDateTime.now().minusDays(5)), 999999);

        when(customerRepository.findByCustomerId(1007L)).thenReturn(Optional.of(customer));
        when(transactionRepository.findAllByCustomerIdAndTransactionDateGreaterThanEqual(eq(1007L), any())).thenReturn(List.of(tx));

        RewardResponseDto result = rewardsService.getRewardsByCustomerId(1007L);

        assertTrue(result.getTotalRewardPoints() > 1_000_000);
    }

    @Test
    void testTransactionOnMonthEnd() {
        Customer customer = new Customer(1003L, "Priyam Goel");

        // Tests if transaction at month-end boundaries are grouped into the correct months

        List<Transaction> txList = List.of(new Transaction(10060L, 1003L, Timestamp.valueOf("2025-06-30 23:59:59"), 120),
                new Transaction(10061L, 1003L, Timestamp.valueOf("2025-05-31 00:00:00"), 130));
        when(customerRepository.findByCustomerId(1003L)).thenReturn(Optional.of(customer));
        when(transactionRepository.findAllByCustomerIdAndTransactionDateGreaterThanEqual(eq(1003L), any())).thenReturn(txList);

        RewardResponseDto result = rewardsService.getRewardsByCustomerId(1003L);

        assertEquals(2, result.getMonthlyRewards().size());
        assertTrue(result.getTotalRewardPoints() > 0);
    }

    @Test
    public void testCustomerNotFound() {

        // Tests that an exception is thrown when the customer is not found

        Long customerId = 9999L;
        when(customerRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> {
            rewardsService.getRewardsByCustomerId(customerId);
        });
    }
}
