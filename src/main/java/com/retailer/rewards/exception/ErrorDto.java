package com.retailer.rewards.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ErrorDto {
    private int status;
    private String error;
    private String message;
    private String path;

}
