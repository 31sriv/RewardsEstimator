package com.retailer.rewards.service;

import com.retailer.rewards.entity.Customer;
import com.retailer.rewards.entity.Transaction;
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
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    public void testEdgeCase_Exactly90DaysAgo() {
        Customer customer = new Customer();
        customer.setCustomerId(1001L);
        customer.setCustomerName("Kriti Sen");

        when(customerRepository.findById(1001L)).thenReturn(Optional.of(customer));

        Transaction tx = new Transaction(10006L, 1001L,
                Timestamp.valueOf(LocalDateTime.now().minusDays(90)), 120);

        when(transactionRepository.findAllByCustomerIdAndTransactionDateGreaterThanEqual(anyLong(), any()))
                .thenReturn(Collections.singletonList(tx));

        RewardResponseDto response = rewardsService.getRewardsByCustomerId(1001L);
        assertEquals(90, response.getTotalRewardPoints());
    }



}
