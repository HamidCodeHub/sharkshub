package com.ucapital.sharkshub.investor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvestorDto {

    private String id;

    @NotBlank(message = "Investor name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;

    private String status;

    @Builder.Default
    private List<String> preferredGeographicalAreas = new ArrayList<>();

    @Builder.Default
    private List<String> preferredInvestmentTypes = new ArrayList<>();

    @Builder.Default
    private List<String> sectors = new ArrayList<>();

    @Builder.Default
    private List<String> verticals = new ArrayList<>();

    @Builder.Default
    private List<String> macroAreas = new ArrayList<>();

    private String type;
    private String macroType;

    @Size(max = 500, message = "Website URL too long")
    private String website;

    private String image;
    private Boolean isOld;

    @Email(message = "Creator email must be valid")
    private String creatorEmail;

    @Email(message = "Admin email must be valid")
    private String adminEmail;

    private Integer completenessScore;

    @Builder.Default
    private Integer impressions = 0;

    @Valid
    private AddressDto hqLocation;

    @Valid
    private FinancialsDto financials;

    @Valid
    private InvDescriptionsDto descriptions;

    @Builder.Default
    private Set<ContactsDto> contacts = new HashSet<>();


}