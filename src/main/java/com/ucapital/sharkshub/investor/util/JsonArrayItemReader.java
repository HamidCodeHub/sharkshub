package com.ucapital.sharkshub.investor.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucapital.sharkshub.investor.dto.InvestorDto;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.core.io.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Custom ItemReader that can handle both JSON arrays and single JSON objects
 */
public class JsonArrayItemReader implements ItemReader<InvestorDto> {

    private static final Logger logger = LoggerFactory.getLogger(JsonArrayItemReader.class);

    private final ObjectMapper objectMapper;
    private Resource resource;
    private Iterator<InvestorDto> iterator;
    private boolean initialized = false;

    public JsonArrayItemReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public InvestorDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!initialized) {
            initialize();
        }

        if (iterator != null && iterator.hasNext()) {
            return iterator.next();
        }

        return null;
    }

    private void initialize() throws IOException {
        if (resource == null || !resource.exists()) {
            throw new IllegalStateException("Resource must be set and exist");
        }

        logger.info("Initializing JsonArrayItemReader with resource: {}", resource.getFilename());

        try {

            List<InvestorDto> investors = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<InvestorDto>>() {}
            );
            iterator = investors.iterator();
            logger.info("Successfully parsed JSON array with {} investors", investors.size());
        } catch (Exception e) {
            logger.warn("Failed to parse as JSON array, trying single object: {}", e.getMessage());
            try {

                InvestorDto singleInvestor = objectMapper.readValue(
                        resource.getInputStream(),
                        InvestorDto.class
                );
                iterator = List.of(singleInvestor).iterator();
                logger.info("Successfully parsed single JSON object");
            } catch (Exception e2) {
                logger.error("Failed to parse JSON as either array or single object", e2);
                throw new IOException("Failed to parse JSON as either array or single object", e2);
            }
        }

        initialized = true;
    }
}