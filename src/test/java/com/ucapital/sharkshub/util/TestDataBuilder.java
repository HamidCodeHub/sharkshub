package com.ucapital.sharkshub.util;


import com.ucapital.sharkshub.investor.dto.*;
import com.ucapital.sharkshub.investor.model.Investor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Builder class for creating test data objects.
 * Provides fluent API for building test entities and DTOs.
 */
public class TestDataBuilder {

    // ========== FACTORY METHODS (Quick Access) ==========

    /**
     * Creates a valid InvestorDto with all required fields populated
     */
    public static InvestorDto createValidInvestorDto() {
        return InvestorDtoBuilder.anInvestorDto().withDefaults().build();
    }

    /**
     * Creates an invalid InvestorDto for testing validation
     */
    public static InvestorDto createInvalidInvestorDto() {
        return InvestorDtoBuilder.anInvestorDto()
                .withName("") // Invalid empty name
                .withStatus("INVALID_STATUS")
                .build();
    }

    /**
     * Creates a valid Investor entity with all required fields populated
     */
    public static Investor createValidInvestor() {
        return InvestorBuilder.anInvestor().withDefaults().build();
    }

    /**
     * Creates a complete InvestorDto with all nested objects populated
     */
    public static InvestorDto createCompleteInvestorDto() {
        return InvestorDtoBuilder.anInvestorDto()
                .withDefaults()
                .withHqLocation(AddressDtoBuilder.anAddressDto().withDefaults().build())
                .withFinancials(FinancialsDtoBuilder.aFinancialsDto().withDefaults().build())
                .withDescriptions(InvDescriptionsDtoBuilder.anInvDescriptionsDto().withDefaults().build())
                .withContacts(Set.of(ContactsDtoBuilder.aContactsDto().withDefaults().build()))
                .build();
    }

    /**
     * Creates a list of InvestorDto with unique names
     */
    public static List<InvestorDto> createInvestorDtoList(int count) {
        List<InvestorDto> investors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            investors.add(InvestorDtoBuilder.anInvestorDto()
                    .withDefaults()
                    .withId("test-id-" + i)
                    .withName("Test Investor " + i)
                    .build());
        }
        return investors;
    }

    /**
     * Creates a list of Investor entities with unique names
     */
    public static List<Investor> createInvestorList(int count) {
        List<Investor> investors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            investors.add(InvestorBuilder.anInvestor()
                    .withDefaults()
                    .withId("test-id-" + i)
                    .withName("Test Investor " + i)
                    .build());
        }
        return investors;
    }

    // ========== INVESTOR DTO BUILDER ==========

    public static class InvestorDtoBuilder {
        private InvestorDto dto = new InvestorDto();

        public static InvestorDtoBuilder anInvestorDto() {
            return new InvestorDtoBuilder();
        }

        public InvestorDtoBuilder withId(String id) {
            dto.setId(id);
            return this;
        }

        public InvestorDtoBuilder withName(String name) {
            dto.setName(name);
            return this;
        }

        public InvestorDtoBuilder withStatus(String status) {
            dto.setStatus(status);
            return this;
        }

        public InvestorDtoBuilder withType(String type) {
            dto.setType(type);
            return this;
        }

        public InvestorDtoBuilder withMacroType(String macroType) {
            dto.setMacroType(macroType);
            return this;
        }

        public InvestorDtoBuilder withWebsite(String website) {
            dto.setWebsite(website);
            return this;
        }

        public InvestorDtoBuilder withImage(String image) {
            dto.setImage(image);
            return this;
        }

        public InvestorDtoBuilder withIsOld(Boolean isOld) {
            dto.setIsOld(isOld);
            return this;
        }

        public InvestorDtoBuilder withCreatorEmail(String creatorEmail) {
            dto.setCreatorEmail(creatorEmail);
            return this;
        }

        public InvestorDtoBuilder withAdminEmail(String adminEmail) {
            dto.setAdminEmail(adminEmail);
            return this;
        }

        public InvestorDtoBuilder withCompletenessScore(Integer completenessScore) {
            dto.setCompletenessScore(completenessScore);
            return this;
        }

        public InvestorDtoBuilder withImpressions(Integer impressions) {
            dto.setImpressions(impressions);
            return this;
        }

        public InvestorDtoBuilder withPreferredGeographicalAreas(List<String> areas) {
            dto.setPreferredGeographicalAreas(areas);
            return this;
        }

        public InvestorDtoBuilder withPreferredInvestmentTypes(List<String> types) {
            dto.setPreferredInvestmentTypes(types);
            return this;
        }

        public InvestorDtoBuilder withSectors(List<String> sectors) {
            dto.setSectors(sectors);
            return this;
        }

        public InvestorDtoBuilder withVerticals(List<String> verticals) {
            dto.setVerticals(verticals);
            return this;
        }

        public InvestorDtoBuilder withMacroAreas(List<String> macroAreas) {
            dto.setMacroAreas(macroAreas);
            return this;
        }

        public InvestorDtoBuilder withHqLocation(AddressDto hqLocation) {
            dto.setHqLocation(hqLocation);
            return this;
        }

        public InvestorDtoBuilder withFinancials(FinancialsDto financials) {
            dto.setFinancials(financials);
            return this;
        }

        public InvestorDtoBuilder withDescriptions(InvDescriptionsDto descriptions) {
            dto.setDescriptions(descriptions);
            return this;
        }

        public InvestorDtoBuilder withContacts(Set<ContactsDto> contacts) {
            dto.setContacts(contacts);
            return this;
        }

        /**
         * Sets sensible default values for all fields
         */
        public InvestorDtoBuilder withDefaults() {
            return withId("test-id-" + UUID.randomUUID().toString().substring(0, 8))
                    .withName("Test Investor Corp")
                    .withStatus("ACTIVE")
                    .withType("VC")
                    .withMacroType("VENTURE_CAPITAL")
                    .withWebsite("https://testinvestor.com")
                    .withImage("test-logo.png")
                    .withIsOld(false)
                    .withCreatorEmail("creator@test.com")
                    .withAdminEmail("admin@test.com")
                    .withCompletenessScore(85)
                    .withImpressions(1000)
                    .withPreferredGeographicalAreas(Arrays.asList("Europe", "North America"))
                    .withPreferredInvestmentTypes(Arrays.asList("Series A", "Series B"))
                    .withSectors(Arrays.asList("FinTech", "HealthTech"))
                    .withVerticals(Arrays.asList("B2B", "SaaS"))
                    .withMacroAreas(Arrays.asList("Technology", "Healthcare"));
        }

        public InvestorDto build() {
            return dto;
        }
    }

    // ========== INVESTOR ENTITY BUILDER ==========

    public static class InvestorBuilder {
        private Investor investor = new Investor();

        public static InvestorBuilder anInvestor() {
            return new InvestorBuilder();
        }

        public InvestorBuilder withId(String id) {
            investor.setId(id);
            return this;
        }

        public InvestorBuilder withName(String name) {
            investor.setName(name);
            return this;
        }

        public InvestorBuilder withStatus(String status) {
            investor.setStatus(status);
            return this;
        }

        public InvestorBuilder withType(String type) {
            investor.setType(type);
            return this;
        }

        public InvestorBuilder withMacroType(String macroType) {
            investor.setMacroType(macroType);
            return this;
        }

        public InvestorBuilder withWebsite(String website) {
            investor.setWebsite(website);
            return this;
        }

        public InvestorBuilder withImage(String image) {
            investor.setImage(image);
            return this;
        }

        public InvestorBuilder withIsOld(Boolean isOld) {
            investor.setIsOld(isOld);
            return this;
        }

        public InvestorBuilder withCreatorEmail(String creatorEmail) {
            investor.setCreatorEmail(creatorEmail);
            return this;
        }

        public InvestorBuilder withAdminEmail(String adminEmail) {
            investor.setAdminEmail(adminEmail);
            return this;
        }

        public InvestorBuilder withCompletenessScore(Integer completenessScore) {
            investor.setCompletenessScore(completenessScore);
            return this;
        }

        public InvestorBuilder withImpressions(Integer impressions) {
            investor.setImpressions(impressions);
            return this;
        }

        public InvestorBuilder withCreatedAt(Instant createdAt) {
            investor.setCreatedAt(createdAt);
            return this;
        }

        public InvestorBuilder withUpdatedAt(Instant updatedAt) {
            investor.setUpdatedAt(updatedAt);
            return this;
        }

        public InvestorBuilder withPreferredGeographicalAreas(List<String> areas) {
            investor.setPreferredGeographicalAreas(areas);
            return this;
        }

        public InvestorBuilder withPreferredInvestmentTypes(List<String> types) {
            investor.setPreferredInvestmentTypes(types);
            return this;
        }

        public InvestorBuilder withSectors(List<String> sectors) {
            investor.setSectors(sectors);
            return this;
        }

        public InvestorBuilder withVerticals(List<String> verticals) {
            investor.setVerticals(verticals);
            return this;
        }

        public InvestorBuilder withMacroAreas(List<String> macroAreas) {
            investor.setMacroAreas(macroAreas);
            return this;
        }

        public InvestorBuilder withHqLocation(Investor.Address hqLocation) {
            investor.setHqLocation(hqLocation);
            return this;
        }

        public InvestorBuilder withFinancials(Investor.Financials financials) {
            investor.setFinancials(financials);
            return this;
        }

        public InvestorBuilder withDescriptions(Investor.InvDescriptions descriptions) {
            investor.setDescriptions(descriptions);
            return this;
        }

        public InvestorBuilder withContacts(Set<Investor.Contacts> contacts) {
            investor.setContacts(contacts);
            return this;
        }

        /**
         * Sets sensible default values for all fields
         */
        public InvestorBuilder withDefaults() {
            Instant now = Instant.now();
            return withId("test-id-" + UUID.randomUUID().toString().substring(0, 8))
                    .withName("Test Investor Corp")
                    .withStatus("ACTIVE")
                    .withType("VC")
                    .withMacroType("VENTURE_CAPITAL")
                    .withWebsite("https://testinvestor.com")
                    .withImage("test-logo.png")
                    .withIsOld(false)
                    .withCreatorEmail("creator@test.com")
                    .withAdminEmail("admin@test.com")
                    .withCompletenessScore(85)
                    .withImpressions(1000)
                    .withCreatedAt(now)
                    .withUpdatedAt(now)
                    .withPreferredGeographicalAreas(Arrays.asList("Europe", "North America"))
                    .withPreferredInvestmentTypes(Arrays.asList("Series A", "Series B"))
                    .withSectors(Arrays.asList("FinTech", "HealthTech"))
                    .withVerticals(Arrays.asList("B2B", "SaaS"))
                    .withMacroAreas(Arrays.asList("Technology", "Healthcare"));
        }

        public Investor build() {
            return investor;
        }
    }

    // ========== ADDRESS DTO BUILDER ==========

    public static class AddressDtoBuilder {
        private AddressDto dto = new AddressDto();

        public static AddressDtoBuilder anAddressDto() {
            return new AddressDtoBuilder();
        }

        public AddressDtoBuilder withAddress(String address) {
            dto.setAddress(address);
            return this;
        }

        public AddressDtoBuilder withCity(String city) {
            dto.setCity(city);
            return this;
        }

        public AddressDtoBuilder withState(String state) {
            dto.setState(state);
            return this;
        }

        public AddressDtoBuilder withZip(String zip) {
            dto.setZip(zip);
            return this;
        }

        public AddressDtoBuilder withCountry(String country) {
            dto.setCountry(country);
            return this;
        }

        public AddressDtoBuilder withPhone(String phone) {
            dto.setPhone(phone);
            return this;
        }

        public AddressDtoBuilder withEmail(String email) {
            dto.setEmail(email);
            return this;
        }

        public AddressDtoBuilder withFax(String fax) {
            dto.setFax(fax);
            return this;
        }

        public AddressDtoBuilder withSn(String sn) {
            dto.setSn(sn);
            return this;
        }

        public AddressDtoBuilder withDefaults() {
            return withAddress("123 Test Street")
                    .withCity("Test City")
                    .withState("Test State")
                    .withZip("12345")
                    .withCountry("Test Country")
                    .withPhone("+1234567890")
                    .withEmail("contact@test.com");
        }

        public AddressDto build() {
            return dto;
        }
    }

    // ========== FINANCIALS DTO BUILDER ==========

    public static class FinancialsDtoBuilder {
        private FinancialsDto dto = new FinancialsDto();

        public static FinancialsDtoBuilder aFinancialsDto() {
            return new FinancialsDtoBuilder();
        }

        public FinancialsDtoBuilder withInvMin(BigDecimal invMin) {
            dto.setInvMin(invMin);
            return this;
        }

        public FinancialsDtoBuilder withInvMax(BigDecimal invMax) {
            dto.setInvMax(invMax);
            return this;
        }

        public FinancialsDtoBuilder withInvAvg(BigDecimal invAvg) {
            dto.setInvAvg(invAvg);
            return this;
        }

        public FinancialsDtoBuilder withDealMax(BigDecimal dealMax) {
            dto.setDealMax(dealMax);
            return this;
        }

        public FinancialsDtoBuilder withDealMin(BigDecimal dealMin) {
            dto.setDealMin(dealMin);
            return this;
        }

        public FinancialsDtoBuilder withCmpValMin(BigDecimal cmpValMin) {
            dto.setCmpValMin(cmpValMin);
            return this;
        }

        public FinancialsDtoBuilder withCmpValMax(BigDecimal cmpValMax) {
            dto.setCmpValMax(cmpValMax);
            return this;
        }

        public FinancialsDtoBuilder withEbitdaMin(BigDecimal ebitdaMin) {
            dto.setEbitdaMin(ebitdaMin);
            return this;
        }

        public FinancialsDtoBuilder withEbitdaMax(BigDecimal ebitdaMax) {
            dto.setEbitdaMax(ebitdaMax);
            return this;
        }

        public FinancialsDtoBuilder withEbitMin(BigDecimal ebitMin) {
            dto.setEbitMin(ebitMin);
            return this;
        }

        public FinancialsDtoBuilder withEbitMax(BigDecimal ebitMax) {
            dto.setEbitMax(ebitMax);
            return this;
        }

        public FinancialsDtoBuilder withDefaults() {
            return withInvMin(BigDecimal.valueOf(100000))
                    .withInvMax(BigDecimal.valueOf(10000000))
                    .withInvAvg(BigDecimal.valueOf(1000000L))
                    .withDealMin(BigDecimal.valueOf(50000))
                    .withDealMax(BigDecimal.valueOf(50000000))
                    .withCmpValMin(BigDecimal.valueOf(1000000))
                    .withCmpValMax(BigDecimal.valueOf(100000000))
                    .withEbitdaMin(BigDecimal.valueOf(100000))
                    .withEbitdaMax(BigDecimal.valueOf(10000000))
                    .withEbitMin(BigDecimal.valueOf(50000))
                    .withEbitMax(BigDecimal.valueOf(5000000));
        }

        public FinancialsDto build() {
            return dto;
        }
    }

    // ========== CONTACTS DTO BUILDER ==========

    public static class ContactsDtoBuilder {
        private ContactsDto dto = new ContactsDto();

        public static ContactsDtoBuilder aContactsDto() {
            return new ContactsDtoBuilder();
        }

        public ContactsDtoBuilder withFirstName(String firstName) {
            dto.setFirstName(firstName);
            return this;
        }

        public ContactsDtoBuilder withLastName(String lastName) {
            dto.setLastName(lastName);
            return this;
        }

        public ContactsDtoBuilder withEmail(String email) {
            dto.setEmail(email);
            return this;
        }

        public ContactsDtoBuilder withPhone(String phone) {
            dto.setPhone(phone);
            return this;
        }

        public ContactsDtoBuilder withMobile(String mobile) {
            dto.setMobile(mobile);
            return this;
        }

        public ContactsDtoBuilder withFax(String fax) {
            dto.setFax(fax);
            return this;
        }

        public ContactsDtoBuilder withRole(String role) {
            dto.setRole(role);
            return this;
        }

        public ContactsDtoBuilder withOrderNum(Integer orderNum) {
            dto.setOrderNum(orderNum);
            return this;
        }

        public ContactsDtoBuilder withDefaults() {
            return withFirstName("John")
                    .withLastName("Doe")
                    .withEmail("john.doe@test.com")
                    .withPhone("+1234567890")
                    .withMobile("+1987654321")
                    .withRole("Partner")
                    .withOrderNum(1);
        }

        public ContactsDto build() {
            return dto;
        }
    }

    // ========== DESCRIPTIONS DTO BUILDER ==========

    public static class InvDescriptionsDtoBuilder {
        private InvDescriptionsDto dto = new InvDescriptionsDto();

        public static InvDescriptionsDtoBuilder anInvDescriptionsDto() {
            return new InvDescriptionsDtoBuilder();
        }

        public InvDescriptionsDtoBuilder withIt(String it) {
            dto.setIt(it);
            return this;
        }

        public InvDescriptionsDtoBuilder withEn(String en) {
            dto.setEn(en);
            return this;
        }

        public InvDescriptionsDtoBuilder withFr(String fr) {
            dto.setFr(fr);
            return this;
        }

        public InvDescriptionsDtoBuilder withDe(String de) {
            dto.setDe(de);
            return this;
        }

        public InvDescriptionsDtoBuilder withEs(String es) {
            dto.setEs(es);
            return this;
        }

        public InvDescriptionsDtoBuilder withRu(String ru) {
            dto.setRu(ru);
            return this;
        }

        public InvDescriptionsDtoBuilder withCh(String ch) {
            dto.setCh(ch);
            return this;
        }

        public InvDescriptionsDtoBuilder withDefaults() {
            return withEn("English description of the investor")
                    .withIt("Descrizione italiana dell'investitore")
                    .withFr("Description française de l'investisseur")
                    .withDe("Deutsche Beschreibung des Investors")
                    .withEs("Descripción española del inversor");
        }

        public InvDescriptionsDto build() {
            return dto;
        }
    }

    // ========== BULK OPERATION RESPONSE BUILDER ==========

    public static class BulkOperationResponseBuilder {
        private BulkOperationResponse response = new BulkOperationResponse();

        public static BulkOperationResponseBuilder aBulkOperationResponse() {
            return new BulkOperationResponseBuilder();
        }

        public BulkOperationResponseBuilder withTotalProcessed(int totalProcessed) {
            response.setTotalProcessed(totalProcessed);
            return this;
        }

        public BulkOperationResponseBuilder withSuccessCount(int successCount) {
            response.setSuccessCount(successCount);
            return this;
        }

        public BulkOperationResponseBuilder withFailureCount(int failureCount) {
            response.setFailureCount(failureCount);
            return this;
        }

        public BulkOperationResponseBuilder withStatus(OperationStatus status) {
            response.setStatus(status);
            return this;
        }

        public BulkOperationResponseBuilder withMessage(String message) {
            response.setMessage(message);
            return this;
        }

        public BulkOperationResponseBuilder withDefaults() {
            return withTotalProcessed(1)
                    .withSuccessCount(1)
                    .withFailureCount(0)
                    .withStatus(OperationStatus.COMPLETED)
                    .withMessage("Operation completed successfully");
        }

        public BulkOperationResponse build() {
            return response;
        }
    }

    // ========== SPECIALIZED FACTORY METHODS ==========

    /**
     * Creates an investor for a specific sector
     */
    public static InvestorDto createInvestorForSector(String sector) {
        return InvestorDtoBuilder.anInvestorDto()
                .withDefaults()
                .withName(sector + " Investor")
                .withSectors(List.of(sector))
                .build();
    }

    /**
     * Creates an inactive investor
     */
    public static InvestorDto createInactiveInvestor() {
        return InvestorDtoBuilder.anInvestorDto()
                .withDefaults()
                .withName("Inactive Investor")
                .withStatus("INACTIVE")
                .build();
    }

    /**
     * Creates an investor with minimal data (only required fields)
     */
    public static InvestorDto createInvestorWithMinimalData() {
        return InvestorDtoBuilder.anInvestorDto()
                .withName("Minimal Investor")
                .withStatus("ACTIVE")
                .withType("VC")
                .build();
    }

    /**
     * Creates an investor specifically for VC type
     */
    public static InvestorDto createVCInvestor() {
        return InvestorDtoBuilder.anInvestorDto()
                .withDefaults()
                .withName("VC Fund")
                .withType("VC")
                .withMacroType("VENTURE_CAPITAL")
                .withPreferredInvestmentTypes(Arrays.asList("Seed", "Series A", "Series B"))
                .build();
    }

    /**
     * Creates an investor specifically for PE type
     */
    public static InvestorDto createPEInvestor() {
        return InvestorDtoBuilder.anInvestorDto()
                .withDefaults()
                .withName("PE Fund")
                .withType("PE")
                .withMacroType("PRIVATE_EQUITY")
                .withPreferredInvestmentTypes(Arrays.asList("Growth", "Buyout"))
                .build();
    }

    /**
     * Creates an investor with European focus
     */
    public static InvestorDto createEuropeanInvestor() {
        return InvestorDtoBuilder.anInvestorDto()
                .withDefaults()
                .withName("European VC")
                .withPreferredGeographicalAreas(Arrays.asList("Europe", "Western Europe"))
                .withHqLocation(AddressDtoBuilder.anAddressDto()
                        .withDefaults()
                        .withCity("London")
                        .withCountry("UK")
                        .build())
                .build();
    }
}