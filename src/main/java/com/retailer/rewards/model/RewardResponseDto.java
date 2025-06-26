package com.retailer.rewards.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RewardResponseDto {

    private String customerName;
    private List<MonthlyRewardDto> monthlyRewards;
    private long totalRewardPoints;
}
