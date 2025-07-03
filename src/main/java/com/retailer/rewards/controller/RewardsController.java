package com.retailer.rewards.controller;

import com.retailer.rewards.model.RewardResponseDto;
import com.retailer.rewards.service.RewardsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customers")
public class RewardsController {

    private final RewardsService rewardsService;

    @GetMapping(value = "/{customerId}/rewards")
    public ResponseEntity<RewardResponseDto> getRewardsByCustomerId(@PathVariable("customerId") Long customerId) {
        RewardResponseDto customerRewards = rewardsService.getRewardsByCustomerId(customerId);
        return new ResponseEntity<>(customerRewards, HttpStatus.OK);
    }
}
