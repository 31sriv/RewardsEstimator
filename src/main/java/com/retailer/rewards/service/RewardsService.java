package com.retailer.rewards.service;

import com.retailer.rewards.model.RewardResponseDto;

public interface RewardsService {
    public RewardResponseDto getRewardsByCustomerId(Long customerId);
}
