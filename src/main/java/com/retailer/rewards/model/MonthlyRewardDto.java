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
public class MonthlyRewardDto {

    private String month;
    private long totalPoints;
    private List<TransactionRewardDto> transactions;
}
