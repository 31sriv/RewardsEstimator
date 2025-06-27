package com.retailer.rewards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailer.rewards.exception.CustomerNotFoundException;
import com.retailer.rewards.model.RewardResponseDto;
import com.retailer.rewards.repository.CustomerRepository;
import com.retailer.rewards.service.RewardsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RewardsController.class)
public class RewardsControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RewardsService rewardsService;

    @MockitoBean
    private CustomerRepository customerRepository;

    @Test
    void testCustomerWithNoTransactions_ReturnsEmptyRewards() throws Exception {
        RewardResponseDto emptyResponse = new RewardResponseDto("Riya", List.of(), 0);

        when(rewardsService.getRewardsByCustomerId(1004L)).thenReturn(emptyResponse);

        mockMvc.perform(get("/customers/1004/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName").value("Riya"))
                .andExpect(jsonPath("$.monthlyRewards").isArray())
                .andExpect(jsonPath("$.monthlyRewards").isEmpty())
                .andExpect(jsonPath("$.totalRewardPoints").value(0));
    }

    @Test
    void testResponseHeaders() throws Exception {

        // To ensure the API is sending back proper headers

        RewardResponseDto dto = new RewardResponseDto("Kriti Sen", List.of(), 120);
        when(rewardsService.getRewardsByCustomerId(1001L)).thenReturn(dto);

        mockMvc.perform(get("/customers/1001/rewards"))
                .andExpect(header().string("Content-Type", "application/json"));
    }

    @Test
    void testGetRewardsByCustomerId_CustomerNotFound() throws Exception {
        Mockito.when(rewardsService.getRewardsByCustomerId(9999L))
                .thenThrow(new CustomerNotFoundException("Customer with ID 9999 not found."));

        mockMvc.perform(get("/customers/9999/rewards"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer with ID 9999 not found."));
    }
}
