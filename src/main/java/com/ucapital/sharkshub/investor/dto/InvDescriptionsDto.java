package com.ucapital.sharkshub.investor.dto;


import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvDescriptionsDto {
    @Size(max = 5000, message = "Italian description too long")
    private String it;

    @Size(max = 5000, message = "English description too long")
    private String en;

    @Size(max = 5000, message = "French description too long")
    private String fr;

    @Size(max = 5000, message = "German description too long")
    private String de;

    @Size(max = 5000, message = "Spanish description too long")
    private String es;

    @Size(max = 5000, message = "Russian description too long")
    private String ru;

    @Size(max = 5000, message = "Chinese description too long")
    private String ch;
}