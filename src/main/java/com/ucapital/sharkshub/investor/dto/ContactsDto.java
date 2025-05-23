package com.ucapital.sharkshub.investor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactsDto {
    @NotBlank(message = "Contact first name is required")
    private String firstName;

    @NotBlank(message = "Contact last name is required")
    private String lastName;

    @Email(message = "Contact email must be valid")
    private String email;

    private String phone;
    private String mobile;
    private String fax;
    private String role;
    private Integer orderNum;
}
