package com.ucapital.sharkshub.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucapital.sharkshub.investor.dto.*;
import com.ucapital.sharkshub.investor.util.JsonArrayItemReader;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.support.RunIdIncrementer;

import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.batch.item.*;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.core.configuration.annotation.StepScope;

import org.springframework.core.io.FileSystemResource;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class InvestorBatchConfig {

    @Bean
    public Job investorBulkJob(JobRepository jobRepository,
                               Step investorBulkStep) {
        return new JobBuilder("investorBulkJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(investorBulkStep)
                .build();
    }

    @Bean
    public Step investorBulkStep(JobRepository jobRepository,
                                 PlatformTransactionManager txManager,
                                 ItemReader<InvestorDto> fileItemReader,
                                 ItemProcessor<InvestorDto, InvestorDto> processor,
                                 ItemWriter<InvestorDto> writer) {
        return new StepBuilder("investorBulkStep", jobRepository)
                .<InvestorDto, InvestorDto>chunk(100, txManager)
                .reader(fileItemReader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(1000)
                .skip(Exception.class)
                .build();
    }

    // Fixed: Delegating reader that handles lifecycle properly
    @Bean
    @StepScope
    public ItemReader<InvestorDto> fileItemReader(@Value("#{jobParameters['filePath']}") String filePath) {
        return new DelegatingFileItemReader(filePath);
    }

    @Bean
    public ItemProcessor<InvestorDto, InvestorDto> processor() {
        return dto -> {
            if (dto != null && dto.getName() != null && !dto.getName().trim().isEmpty()) {
                return dto;
            }
            return null;
        };
    }

    @Bean
    public MongoItemWriter<InvestorDto> writer(MongoTemplate template) {
        return new MongoItemWriterBuilder<InvestorDto>()
                .template(template)
                .collection("investors")
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(2);
        exec.setMaxPoolSize(4);
        exec.setQueueCapacity(10);
        exec.setThreadNamePrefix("bulk-file-");
        exec.initialize();
        return exec;
    }

    // Custom delegating reader with lazy initialization
    public static class DelegatingFileItemReader implements ItemReader<InvestorDto>, ItemStream {
        private final String filePath;
        private ItemReader<InvestorDto> delegate;
        private ItemStream streamDelegate;
        private boolean initialized = false;
        private ExecutionContext executionContext;

        public DelegatingFileItemReader(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void open(ExecutionContext executionContext) throws ItemStreamException {
            System.out.println("=== DelegatingFileItemReader.open() called for: " + filePath + " ===");
            this.executionContext = executionContext;
            initializeIfNeeded();
        }

        @Override
        public void update(ExecutionContext executionContext) throws ItemStreamException {
            if (streamDelegate != null) {
                streamDelegate.update(executionContext);
            }
        }

        @Override
        public void close() throws ItemStreamException {
            System.out.println("=== DelegatingFileItemReader.close() called ===");
            if (streamDelegate != null) {
                streamDelegate.close();
            }
        }

        @Override
        public InvestorDto read() throws Exception {
            initializeIfNeeded();
            return delegate.read();
        }

        private void initializeIfNeeded() {
            if (!initialized) {
                System.out.println("=== Initializing reader for file: " + filePath + " ===");

                if (filePath != null && filePath.toLowerCase().endsWith(".json")) {
                    // JSON Reader
                    System.out.println("=== Creating JSON reader ===");
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonArrayItemReader jsonReader = new JsonArrayItemReader(objectMapper);
                    jsonReader.setResource(new FileSystemResource(filePath));
                    delegate = jsonReader;
                    streamDelegate = null; // JsonArrayItemReader doesn't implement ItemStream


                } else {
                    // CSV Reader
                    System.out.println("=== Creating CSV reader ===");
                    FlatFileItemReader<InvestorDto> csvReader = createCsvReader();
                    delegate = csvReader;
                    streamDelegate = csvReader; // FlatFileItemReader implements ItemStream

                    // Open the CSV reader if we have an execution context
                    if (streamDelegate != null && executionContext != null) {
                        try {
                            streamDelegate.open(executionContext);

                        } catch (Exception e) {
                            System.err.println("=== Error opening CSV reader: " + e.getMessage() + " ===");
                            throw new RuntimeException("Failed to open CSV reader", e);
                        }
                    }
                }

                initialized = true;
                System.out.println("=== Reader initialization completed ===");
            }
        }

        private FlatFileItemReader<InvestorDto> createCsvReader() {
            FlatFileItemReader<InvestorDto> reader = new FlatFileItemReader<>();
            reader.setResource(new FileSystemResource(filePath));
            reader.setLinesToSkip(1);

            DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
            tokenizer.setDelimiter(DelimitedLineTokenizer.DELIMITER_COMMA);
            tokenizer.setQuoteCharacter('"');
            tokenizer.setNames(new String[]{
                    "name","status","preferredGeographicalAreas","preferredInvestmentTypes","sectors","verticals",
                    "macroAreas","type","macroType","website","image","isOld","creatorEmail","adminEmail",
                    "completenessScore","impressions","hqAddress","hqCity","hqState","hqZip","hqCountry",
                    "hqPhone","hqEmail","hqFax","hqSn","invMin","invMax","invAvg","dealMax","dealMin",
                    "cmpValMin","cmpValMax","ebitdaMin","ebitdaMax","ebitMin","ebitMax",
                    "desc_it","desc_en","desc_fr","desc_de","desc_es","desc_ru","desc_ch",
                    "contact_firstName","contact_lastName","contact_email","contact_phone",
                    "contact_mobile","contact_fax","contact_role","contact_orderNum"
            });
            tokenizer.setStrict(false);

            DefaultLineMapper<InvestorDto> lineMapper = new DefaultLineMapper<>();
            lineMapper.setLineTokenizer(tokenizer);
            lineMapper.setFieldSetMapper(fieldSet -> {
                try {
                    List<String> geoAreas = splitToList(readStringSafe(fieldSet, "preferredGeographicalAreas"));
                    List<String> invTypes = splitToList(readStringSafe(fieldSet, "preferredInvestmentTypes"));
                    List<String> sectors = splitToList(readStringSafe(fieldSet, "sectors"));
                    List<String> verticals = splitToList(readStringSafe(fieldSet, "verticals"));
                    List<String> macroAreas = splitToList(readStringSafe(fieldSet, "macroAreas"));

                    AddressDto address = AddressDto.builder()
                            .address(readStringSafe(fieldSet, "hqAddress"))
                            .city(readStringSafe(fieldSet, "hqCity"))
                            .state(readStringSafe(fieldSet, "hqState"))
                            .zip(readStringSafe(fieldSet, "hqZip"))
                            .country(readStringSafe(fieldSet, "hqCountry"))
                            .phone(readStringSafe(fieldSet, "hqPhone"))
                            .email(readStringSafe(fieldSet, "hqEmail"))
                            .fax(readStringSafe(fieldSet, "hqFax"))
                            .sn(readStringSafe(fieldSet, "hqSn"))
                            .build();

                    FinancialsDto fin = FinancialsDto.builder()
                            .invMin(readBigDecimalSafe(fieldSet, "invMin"))
                            .invMax(readBigDecimalSafe(fieldSet, "invMax"))
                            .invAvg(readBigDecimalSafe(fieldSet, "invAvg"))
                            .dealMax(readBigDecimalSafe(fieldSet, "dealMax"))
                            .dealMin(readBigDecimalSafe(fieldSet, "dealMin"))
                            .cmpValMin(readBigDecimalSafe(fieldSet, "cmpValMin"))
                            .cmpValMax(readBigDecimalSafe(fieldSet, "cmpValMax"))
                            .ebitdaMin(readBigDecimalSafe(fieldSet, "ebitdaMin"))
                            .ebitdaMax(readBigDecimalSafe(fieldSet, "ebitdaMax"))
                            .ebitMin(readBigDecimalSafe(fieldSet, "ebitMin"))
                            .ebitMax(readBigDecimalSafe(fieldSet, "ebitMax"))
                            .build();

                    InvDescriptionsDto desc = InvDescriptionsDto.builder()
                            .it(readStringSafe(fieldSet, "desc_it"))
                            .en(readStringSafe(fieldSet, "desc_en"))
                            .fr(readStringSafe(fieldSet, "desc_fr"))
                            .de(readStringSafe(fieldSet, "desc_de"))
                            .es(readStringSafe(fieldSet, "desc_es"))
                            .ru(readStringSafe(fieldSet, "desc_ru"))
                            .ch(readStringSafe(fieldSet, "desc_ch"))
                            .build();

                    ContactsDto contact = ContactsDto.builder()
                            .firstName(readStringSafe(fieldSet, "contact_firstName"))
                            .lastName(readStringSafe(fieldSet, "contact_lastName"))
                            .email(readStringSafe(fieldSet, "contact_email"))
                            .phone(readStringSafe(fieldSet, "contact_phone"))
                            .mobile(readStringSafe(fieldSet, "contact_mobile"))
                            .fax(readStringSafe(fieldSet, "contact_fax"))
                            .role(readStringSafe(fieldSet, "contact_role"))
                            .orderNum(readIntSafe(fieldSet, "contact_orderNum"))
                            .build();

                    return InvestorDto.builder()
                            .name(readStringSafe(fieldSet, "name"))
                            .status(readStringSafe(fieldSet, "status"))
                            .preferredGeographicalAreas(geoAreas)
                            .preferredInvestmentTypes(invTypes)
                            .sectors(sectors)
                            .verticals(verticals)
                            .macroAreas(macroAreas)
                            .type(readStringSafe(fieldSet, "type"))
                            .macroType(readStringSafe(fieldSet, "macroType"))
                            .website(readStringSafe(fieldSet, "website"))
                            .image(readStringSafe(fieldSet, "image"))
                            .isOld(readBooleanSafe(fieldSet, "isOld"))
                            .creatorEmail(readStringSafe(fieldSet, "creatorEmail"))
                            .adminEmail(readStringSafe(fieldSet, "adminEmail"))
                            .completenessScore(readIntSafe(fieldSet, "completenessScore"))
                            .impressions(readIntSafe(fieldSet, "impressions"))
                            .hqLocation(address)
                            .financials(fin)
                            .descriptions(desc)
                            .contacts(new HashSet<>(List.of(contact)))
                            .build();
                } catch (Exception e) {
                    System.err.println("=== CSV FIELD MAPPING ERROR: " + e.getMessage() + " ===");
                    throw new RuntimeException("Error processing CSV row: " + e.getMessage(), e);
                }
            });

            reader.setLineMapper(lineMapper);
            return reader;
        }

        // Helper methods for safe field reading
        private String readStringSafe(org.springframework.batch.item.file.transform.FieldSet fieldSet, String fieldName) {
            try {
                String value = fieldSet.readString(fieldName);
                return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
            } catch (Exception e) {
                return null;
            }
        }

        private java.math.BigDecimal readBigDecimalSafe(org.springframework.batch.item.file.transform.FieldSet fieldSet, String fieldName) {
            try {
                String value = fieldSet.readString(fieldName);
                if (value == null || value.trim().isEmpty()) {
                    return null;
                }
                return fieldSet.readBigDecimal(fieldName);
            } catch (Exception e) {
                return null;
            }
        }

        private Integer readIntSafe(org.springframework.batch.item.file.transform.FieldSet fieldSet, String fieldName) {
            try {
                String value = fieldSet.readString(fieldName);
                if (value == null || value.trim().isEmpty()) {
                    return null;
                }
                return fieldSet.readInt(fieldName);
            } catch (Exception e) {
                return null;
            }
        }

        private Boolean readBooleanSafe(org.springframework.batch.item.file.transform.FieldSet fieldSet, String fieldName) {
            try {
                String value = fieldSet.readString(fieldName);
                if (value == null || value.trim().isEmpty()) {
                    return null;
                }
                return fieldSet.readBoolean(fieldName);
            } catch (Exception e) {
                return false;
            }
        }

        private List<String> splitToList(String raw) {
            if (raw == null || raw.isBlank()) {
                return Collections.emptyList();
            }
            return Arrays.stream(raw.split("[;,]"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
    }
}