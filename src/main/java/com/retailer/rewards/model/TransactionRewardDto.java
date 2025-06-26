package com.retailer.rewards.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRewardDto {
    private long transactionId;
    private double amount;
    private long rewardPoints;
}
