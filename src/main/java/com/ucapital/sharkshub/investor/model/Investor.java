package com.ucapital.sharkshub.investor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "investors")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Investor {
    @Id
    private String id;

    @Indexed
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
    private String website;
    private String image;
    private Boolean isOld;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Email
    private String creatorEmail;

    @Email
    private String adminEmail;

    @Indexed(direction = IndexDirection.DESCENDING)
    private Integer completenessScore;

    @Builder.Default
    private Integer impressions = 0;

    private Address hqLocation;
    private Financials financials;
    private InvDescriptions descriptions;
    @Builder.Default
    private Set<Contacts> contacts = new HashSet<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String address;
        private String city;
        private String state;
        private String zip;
        private String country;
        private String phone;
        private String email;
        private String fax;
        private String sn;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Financials {
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvDescriptions {
        private String it;
        private String en;
        private String fr;
        private String de;
        private String es;
        private String ru;
        private String ch;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contacts {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String mobile;
        private String fax;
        private String role;
        private Integer orderNum;
    }
}
