package com.ucapital.sharkshub.investor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialsDto {
    private BigDecimal invMin;
    private BigDecimal invMax;
    private BigDecimal invAvg;
    private BigDecimal dealMax;
    private BigDecimal dealMin;
    private BigDecimal cmpValMin;
    private BigDecimal cmpValMax;
    private BigDecimal ebitdaMin;
    private BigDecimal ebitdaMax;
    private BigDecimal ebitMin;
    private BigDecimal ebitMax;
}
