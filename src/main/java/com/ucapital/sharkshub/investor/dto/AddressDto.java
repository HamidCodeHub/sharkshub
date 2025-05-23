package com.ucapital.sharkshub.investor.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private String address;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String phone;

    @Email(message = "Address email must be valid")
    private String email;

    private String fax;
    private String sn;
}