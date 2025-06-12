package com.ucapital.sharkshub.investor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class JobLaunchResponse {
    private long jobExecutionId;
    private String statusUrl;
}