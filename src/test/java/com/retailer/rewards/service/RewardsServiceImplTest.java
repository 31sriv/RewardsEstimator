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
public class RewardsServiceImplTest {
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private RewardsServiceImpl rewardsService;

    private Customer customer;

    @Test
    public void testGetRewardsByCustomerId_Success() {
        Customer customer = new Customer();
        customer.setCustomerId(1001L);
        customer.setCustomerName("Kriti Sen");

        when(customerRepository.findByCustomerId(1001L)).thenReturn(Optional.of(customer));

        List<Transaction> transactions = Arrays.asList(
                new Transaction(10001L, 1001L, Timestamp.valueOf(LocalDateTime.now().minusDays(20)), 90),
                new Transaction(10002L, 1001L, Timestamp.valueOf(LocalDateTime.now().minusDays(15)), 190)
        );

        when(transactionRepository.findAllByCustomerIdAndTransactionDateGreaterThanEqual(
                eq(1001L), any(Timestamp.class))
        ).thenReturn(transactions);

        RewardResponseDto response = rewardsService.getRewardsByCustomerId(1001L);

        assertEquals("Kriti Sen", response.getCustomerName());

        assertEquals(2, response.getMonthlyRewards().get(0).getTransactions().size());
        assertTrue(response.getTotalRewardPoints() > 0);
    }

    @Test
    public void testEdgeCase_Exactly90DaysAgo() {
        Customer customer = new Customer(1001L, "Kriti Sen");

        when(customerRepository.findById(1001L)).thenReturn(Optional.of(customer));

        Transaction tx = new Transaction(10006L, 1001L,
                Timestamp.valueOf(LocalDateTime.now().minusDays(90)), 120);

        when(transactionRepository.findAllByCustomerIdAndTransactionDateGreaterThanEqual(anyLong(), any()))
                .thenReturn(List.of(tx));

        RewardResponseDto response = rewardsService.getRewardsByCustomerId(1001L);
        assertEquals("Kriti Sen", response.getCustomerName());
        assertEquals(90, response.getTotalRewardPoints());
    }

    @Test
    void testVeryLargeTransactionAmount() {
        Customer customer = new Customer(1007L, "Kriti Sen");

        Transaction tx = new Transaction(10050L, 1007L, Timestamp.valueOf(LocalDateTime.now().minusDays(5)), 999999);

        when(customerRepository.findById(1007L)).thenReturn(Optional.of(customer));
        when(transactionRepository.findAllByCustomerIdAndTransactionDateGreaterThanEqual(eq(1007L), any()))
                .thenReturn(List.of(tx));

        RewardResponseDto result = rewardsService.getRewardsByCustomerId(1007L);

        assertTrue(result.getTotalRewardPoints() > 1_000_000);
    }

    @Test
    void testTransactionOnMonthEnd() {
        Customer customer = new Customer(10031L, "Priyam Goel");

        List<Transaction> txList = List.of(
                new Transaction(10060L, 10031L, Timestamp.valueOf("2025-06-30 23:59:59"), 120),
                new Transaction(10061L, 10031L, Timestamp.valueOf("2025-05-31 00:00:00"), 130)
        );

        when(customerRepository.findById(10031L)).thenReturn(Optional.of(customer));
        when(transactionRepository.findAllByCustomerIdAndTransactionDateGreaterThanEqual(eq(10031L), any()))
                .thenReturn(txList);

        RewardResponseDto result = rewardsService.getRewardsByCustomerId(10031L);

        assertEquals(2, result.getMonthlyRewards().size());
        assertTrue(result.getTotalRewardPoints() > 0);
    }

    @Test
    public void testCustomerNotFound() {
        Long customerId = 9999L;
        when(customerRepository.findByCustomerId(customerId)).thenReturn(null);

        assertThrows(CustomerNotFoundException.class, () -> {
            rewardsService.getRewardsByCustomerId(customerId);
        });
    }

}
