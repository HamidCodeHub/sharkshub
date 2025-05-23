package com.ucapital.sharkshub.investor.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucapital.sharkshub.investor.dto.AddressDto;
import com.ucapital.sharkshub.investor.dto.ContactsDto;
import com.ucapital.sharkshub.investor.dto.FinancialsDto;
import com.ucapital.sharkshub.investor.dto.InvDescriptionsDto;
import com.ucapital.sharkshub.investor.dto.InvestorDto;
import com.ucapital.sharkshub.investor.exception.InvestorValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;


@Component
public class FileParser {

    private static final Logger logger = LoggerFactory.getLogger(FileParser.class);
    private static final String CSV_CONTENT_TYPE = "text/csv";
    private static final String CSV_ALTERNATIVE_TYPE = "application/vnd.ms-excel";
    private static final String JSON_CONTENT_TYPE = "application/json";

    private final ObjectMapper objectMapper;

    public FileParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public List<InvestorDto> parseFile(MultipartFile file) throws IOException, InvestorValidationException {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        logger.info("Parsing file: {}, content type: {}", filename, contentType);

        if (contentType == null) {
            if (filename != null && filename.toLowerCase().endsWith(".csv")) {
                contentType = CSV_CONTENT_TYPE;
            } else if (filename != null && filename.toLowerCase().endsWith(".json")) {
                contentType = JSON_CONTENT_TYPE;
            } else {
                throw new InvestorValidationException("Cannot determine file type. Supported formats: CSV, JSON");
            }
        }

        if (contentType.equals(CSV_CONTENT_TYPE) || contentType.equals(CSV_ALTERNATIVE_TYPE) ||
                (filename != null && filename.toLowerCase().endsWith(".csv"))) {
            return parseCsv(file.getInputStream());
        } else if (contentType.equals(JSON_CONTENT_TYPE) ||
                (filename != null && filename.toLowerCase().endsWith(".json"))) {
            return parseJson(file.getInputStream());
        } else {
            throw new InvestorValidationException("Unsupported file format: " + contentType
                    + ". Supported formats: CSV, JSON");
        }
    }


    private List<InvestorDto> parseCsv(InputStream inputStream) throws IOException {
        List<InvestorDto> investors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }

            String[] headers = headerLine.split(",");
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim();
            }

            String line;
            int rowNumber = 1;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                Map<String, String> row = new HashMap<>();
                String[] values = parseCsvLine(line);

                if (values.length != headers.length) {
                    logger.warn("Row {} has {} values, expected {} (headers). Row will be processed with available data.",
                            rowNumber, values.length, headers.length);
                }

                for (int i = 0; i < Math.min(headers.length, values.length); i++) {
                    row.put(headers[i], values[i]);
                }

                InvestorDto investor = convertMapToInvestorDto(row);
                investors.add(investor);

                rowNumber++;
            }
        }

        logger.info("Parsed {} investors from CSV", investors.size());

        return investors;
    }


    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentValue.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(currentValue.toString().trim());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }

        result.add(currentValue.toString().trim());

        return result.toArray(new String[0]);
    }

    private InvestorDto convertMapToInvestorDto(Map<String, String> row) {
        InvestorDto.InvestorDtoBuilder builder = InvestorDto.builder();

        if (row.containsKey("id")) builder.id(row.get("id"));
        if (row.containsKey("name")) builder.name(row.get("name"));
        if (row.containsKey("status")) builder.status(row.get("status"));
        if (row.containsKey("type")) builder.type(row.get("type"));
        if (row.containsKey("macroType")) builder.macroType(row.get("macroType"));
        if (row.containsKey("website")) builder.website(row.get("website"));
        if (row.containsKey("image")) builder.image(row.get("image"));
        if (row.containsKey("creatorEmail")) builder.creatorEmail(row.get("creatorEmail"));
        if (row.containsKey("adminEmail")) builder.adminEmail(row.get("adminEmail"));

        if (row.containsKey("isOld") && row.get("isOld") != null) {
            builder.isOld(Boolean.parseBoolean(row.get("isOld")));
        }

        if (row.containsKey("completenessScore") && row.get("completenessScore") != null) {
            try {
                builder.completenessScore(Integer.parseInt(row.get("completenessScore")));
            } catch (NumberFormatException e) {
                logger.warn("Invalid completenessScore: {}", row.get("completenessScore"));
            }
        }

        if (row.containsKey("impressions") && row.get("impressions") != null) {
            try {
                builder.impressions(Integer.parseInt(row.get("impressions")));
            } catch (NumberFormatException e) {
                logger.warn("Invalid impressions: {}", row.get("impressions"));
            }
        }

        if (row.containsKey("preferredGeographicalAreas")) {
            builder.preferredGeographicalAreas(parseListField(row.get("preferredGeographicalAreas")));
        }

        if (row.containsKey("preferredInvestmentTypes")) {
            builder.preferredInvestmentTypes(parseListField(row.get("preferredInvestmentTypes")));
        }

        if (row.containsKey("sectors")) {
            builder.sectors(parseListField(row.get("sectors")));
        }

        if (row.containsKey("verticals")) {
            builder.verticals(parseListField(row.get("verticals")));
        }

        if (row.containsKey("macroAreas")) {
            builder.macroAreas(parseListField(row.get("macroAreas")));
        }

        mapHqLocation(row, builder);
        mapFinancials(row, builder);
        mapDescriptions(row, builder);
        mapContacts(row, builder);

        return builder.build();
    }


    private List<String> parseListField(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(Arrays.asList(value.split("\\|")));
    }


    private void mapHqLocation(Map<String, String> row, InvestorDto.InvestorDtoBuilder builder) {
        boolean hasHqLocation = row.keySet().stream()
                .anyMatch(key -> key.startsWith("hqLocation."));

        if (!hasHqLocation) {
            return;
        }

        AddressDto.AddressDtoBuilder addressBuilder = AddressDto.builder();

        if (row.containsKey("hqLocation.address")) addressBuilder.address(row.get("hqLocation.address"));
        if (row.containsKey("hqLocation.city")) addressBuilder.city(row.get("hqLocation.city"));
        if (row.containsKey("hqLocation.state")) addressBuilder.state(row.get("hqLocation.state"));
        if (row.containsKey("hqLocation.zip")) addressBuilder.zip(row.get("hqLocation.zip"));
        if (row.containsKey("hqLocation.country")) addressBuilder.country(row.get("hqLocation.country"));
        if (row.containsKey("hqLocation.phone")) addressBuilder.phone(row.get("hqLocation.phone"));
        if (row.containsKey("hqLocation.email")) addressBuilder.email(row.get("hqLocation.email"));
        if (row.containsKey("hqLocation.fax")) addressBuilder.fax(row.get("hqLocation.fax"));
        if (row.containsKey("hqLocation.sn")) addressBuilder.sn(row.get("hqLocation.sn"));

        builder.hqLocation(addressBuilder.build());
    }


    private void mapFinancials(Map<String, String> row, InvestorDto.InvestorDtoBuilder builder) {
        // Check if there are any financials fields
        boolean hasFinancials = row.keySet().stream()
                .anyMatch(key -> key.startsWith("financials."));

        if (!hasFinancials) {
            return;
        }

        FinancialsDto.FinancialsDtoBuilder financialsBuilder = FinancialsDto.builder();

        mapBigDecimalField(row, "financials.invMin", financialsBuilder::invMin);
        mapBigDecimalField(row, "financials.invMax", financialsBuilder::invMax);
        mapBigDecimalField(row, "financials.invAvg", financialsBuilder::invAvg);
        mapBigDecimalField(row, "financials.dealMax", financialsBuilder::dealMax);
        mapBigDecimalField(row, "financials.dealMin", financialsBuilder::dealMin);
        mapBigDecimalField(row, "financials.cmpValMin", financialsBuilder::cmpValMin);
        mapBigDecimalField(row, "financials.cmpValMax", financialsBuilder::cmpValMax);
        mapBigDecimalField(row, "financials.ebitdaMin", financialsBuilder::ebitdaMin);
        mapBigDecimalField(row, "financials.ebitdaMax", financialsBuilder::ebitdaMax);
        mapBigDecimalField(row, "financials.ebitMin", financialsBuilder::ebitMin);
        mapBigDecimalField(row, "financials.ebitMax", financialsBuilder::ebitMax);

        builder.financials(financialsBuilder.build());
    }


    private void mapBigDecimalField(Map<String, String> row, String fieldName,
                                    Function<BigDecimal, FinancialsDto.FinancialsDtoBuilder> setter) {
        if (row.containsKey(fieldName) && row.get(fieldName) != null && !row.get(fieldName).isEmpty()) {
            try {
                setter.apply(new BigDecimal(row.get(fieldName)));
            } catch (NumberFormatException e) {
                logger.warn("Invalid number format for field {}: {}", fieldName, row.get(fieldName));
            }
        }
    }


    private void mapDescriptions(Map<String, String> row, InvestorDto.InvestorDtoBuilder builder) {
        boolean hasDescriptions = row.keySet().stream()
                .anyMatch(key -> key.startsWith("descriptions."));

        if (!hasDescriptions) {
            return;
        }

        InvDescriptionsDto.InvDescriptionsDtoBuilder descriptionsBuilder = InvDescriptionsDto.builder();

        if (row.containsKey("descriptions.it")) descriptionsBuilder.it(row.get("descriptions.it"));
        if (row.containsKey("descriptions.en")) descriptionsBuilder.en(row.get("descriptions.en"));
        if (row.containsKey("descriptions.fr")) descriptionsBuilder.fr(row.get("descriptions.fr"));
        if (row.containsKey("descriptions.de")) descriptionsBuilder.de(row.get("descriptions.de"));
        if (row.containsKey("descriptions.es")) descriptionsBuilder.es(row.get("descriptions.es"));
        if (row.containsKey("descriptions.ru")) descriptionsBuilder.ru(row.get("descriptions.ru"));
        if (row.containsKey("descriptions.ch")) descriptionsBuilder.ch(row.get("descriptions.ch"));

        builder.descriptions(descriptionsBuilder.build());
    }


    private void mapContacts(Map<String, String> row, InvestorDto.InvestorDtoBuilder builder) {
        Map<Integer, Map<String, String>> contactsMap = new HashMap<>();

        for (Map.Entry<String, String> entry : row.entrySet()) {
            if (entry.getKey().startsWith("contacts[")) {
                String key = entry.getKey();
                int startIndex = key.indexOf('[') + 1;
                int endIndex = key.indexOf(']');

                if (startIndex > 0 && endIndex > startIndex) {
                    try {
                        int index = Integer.parseInt(key.substring(startIndex, endIndex));
                        String fieldName = key.substring(endIndex + 2);

                        contactsMap.computeIfAbsent(index, k -> new HashMap<>())
                                .put(fieldName, entry.getValue());
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid contact index in key: {}", key);
                    }
                }
            }
        }

        Set<ContactsDto> contacts = new HashSet<>();

        for (Map.Entry<Integer, Map<String, String>> contactEntry : contactsMap.entrySet()) {
            Map<String, String> contactFields = contactEntry.getValue();

            ContactsDto.ContactsDtoBuilder contactBuilder = ContactsDto.builder();

            if (contactFields.containsKey("firstName"))
                contactBuilder.firstName(contactFields.get("firstName"));

            if (contactFields.containsKey("lastName"))
                contactBuilder.lastName(contactFields.get("lastName"));

            if (contactFields.containsKey("email"))
                contactBuilder.email(contactFields.get("email"));

            if (contactFields.containsKey("phone"))
                contactBuilder.phone(contactFields.get("phone"));

            if (contactFields.containsKey("mobile"))
                contactBuilder.mobile(contactFields.get("mobile"));

            if (contactFields.containsKey("fax"))
                contactBuilder.fax(contactFields.get("fax"));

            if (contactFields.containsKey("role"))
                contactBuilder.role(contactFields.get("role"));

            if (contactFields.containsKey("orderNum") && contactFields.get("orderNum") != null) {
                try {
                    contactBuilder.orderNum(Integer.parseInt(contactFields.get("orderNum")));
                } catch (NumberFormatException e) {
                    logger.warn("Invalid orderNum for contact: {}", contactFields.get("orderNum"));
                }
            }

            ContactsDto contact = contactBuilder.build();
            if (contact.getFirstName() != null || contact.getLastName() != null) {
                contacts.add(contact);
            }
        }

        if (!contacts.isEmpty()) {
            builder.contacts(contacts);
        }
    }


    private List<InvestorDto> parseJson(InputStream inputStream) throws IOException {
        List<InvestorDto> investors;

        try {
            investors = Arrays.asList(objectMapper.readValue(inputStream, InvestorDto[].class));
        } catch (Exception e) {
            investors = new ArrayList<>();
            try {
                InvestorDto investor = objectMapper.readValue(inputStream, InvestorDto.class);
                investors.add(investor);
            } catch (Exception e2) {
                logger.error("Failed to parse JSON as investor(s)", e2);
                throw new IOException("Failed to parse JSON: " + e2.getMessage(), e2);
            }
        }

        logger.info("Parsed {} investors from JSON", investors.size());

        return investors;
    }
}