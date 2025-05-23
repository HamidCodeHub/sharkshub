package com.ucapital.sharkshub.investor.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkError {

    private int itemIndex;
    private String investorName;
    private String errorCode;
    private String errorMessage;
    private String fieldName;
    private Object rejectedValue;
}