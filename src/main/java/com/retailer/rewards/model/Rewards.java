package com.retailer.rewards.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Rewards {
    private long customerId;
    private long lastMonthReward;
    private long lastSecondMonthReward;
    private long lastThirdMonthReward;
    private long totalRewards;

}

