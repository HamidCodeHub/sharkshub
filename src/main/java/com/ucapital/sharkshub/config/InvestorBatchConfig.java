package com.ucapital.sharkshub.config;

import com.ucapital.sharkshub.investor.dto.*;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.support.RunIdIncrementer;

import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.core.configuration.annotation.StepScope;

import org.springframework.core.io.FileSystemResource;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.item.json.JacksonJsonObjectReader;

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
                                 ItemReader<InvestorDto> reader,
                                 ItemProcessor<InvestorDto, InvestorDto> processor,
                                 ItemWriter<InvestorDto> writer,
                                 TaskExecutor taskExecutor) {
        return new StepBuilder("investorBulkStep", jobRepository)
                .<InvestorDto, InvestorDto>chunk(100, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(1000)
                .skip(Exception.class)
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<InvestorDto> reader(
            @Value("#{jobParameters['filePath']}") String filePath,
            FlatFileItemReader<InvestorDto> csvReader,
            JsonItemReader<InvestorDto> jsonReader
    ) {
        if (filePath.toLowerCase().endsWith(".json")) {
            return jsonReader;
        } else {
            return csvReader;
        }
    }

    @Bean
    @StepScope
    public FlatFileItemReader<InvestorDto> csvReader(
            @Value("#{jobParameters['filePath']}") String filePath
    ) {
        FlatFileItemReader<InvestorDto> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(filePath));
        reader.setLinesToSkip(1);


        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(DelimitedLineTokenizer.DELIMITER_COMMA);
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

            List<String> geoAreas = splitToList(fieldSet.readString("preferredGeographicalAreas"));
            List<String> invTypes = splitToList(fieldSet.readString("preferredInvestmentTypes"));
            List<String> sectors   = splitToList(fieldSet.readString("sectors"));
            List<String> verticals = splitToList(fieldSet.readString("verticals"));
            List<String> macroAreas= splitToList(fieldSet.readString("macroAreas"));


            AddressDto address = AddressDto.builder()
                    .address(fieldSet.readString("hqAddress"))
                    .city(fieldSet.readString("hqCity"))
                    .state(fieldSet.readString("hqState"))
                    .zip(fieldSet.readString("hqZip"))
                    .country(fieldSet.readString("hqCountry"))
                    .phone(fieldSet.readString("hqPhone"))
                    .email(fieldSet.readString("hqEmail"))
                    .fax(fieldSet.readString("hqFax"))
                    .sn(fieldSet.readString("hqSn"))
                    .build();


            FinancialsDto fin = FinancialsDto.builder()
                    .invMin(fieldSet.readBigDecimal("invMin"))
                    .invMax(fieldSet.readBigDecimal("invMax"))
                    .invAvg(fieldSet.readBigDecimal("invAvg"))
                    .dealMax(fieldSet.readBigDecimal("dealMax"))
                    .dealMin(fieldSet.readBigDecimal("dealMin"))
                    .cmpValMin(fieldSet.readBigDecimal("cmpValMin"))
                    .cmpValMax(fieldSet.readBigDecimal("cmpValMax"))
                    .ebitdaMin(fieldSet.readBigDecimal("ebitdaMin"))
                    .ebitdaMax(fieldSet.readBigDecimal("ebitdaMax"))
                    .ebitMin(fieldSet.readBigDecimal("ebitMin"))
                    .ebitMax(fieldSet.readBigDecimal("ebitMax"))
                    .build();


            InvDescriptionsDto desc = InvDescriptionsDto.builder()
                    .it(fieldSet.readString("desc_it"))
                    .en(fieldSet.readString("desc_en"))
                    .fr(fieldSet.readString("desc_fr"))
                    .de(fieldSet.readString("desc_de"))
                    .es(fieldSet.readString("desc_es"))
                    .ru(fieldSet.readString("desc_ru"))
                    .ch(fieldSet.readString("desc_ch"))
                    .build();


            ContactsDto contact = ContactsDto.builder()
                    .firstName(fieldSet.readString("contact_firstName"))
                    .lastName(fieldSet.readString("contact_lastName"))
                    .email(fieldSet.readString("contact_email"))
                    .phone(fieldSet.readString("contact_phone"))
                    .mobile(fieldSet.readString("contact_mobile"))
                    .fax(fieldSet.readString("contact_fax"))
                    .role(fieldSet.readString("contact_role"))
                    .orderNum(fieldSet.readInt("contact_orderNum"))
                    .build();


            return InvestorDto.builder()
                    .name(fieldSet.readString("name"))
                    .status(fieldSet.readString("status"))
                    .preferredGeographicalAreas(geoAreas)
                    .preferredInvestmentTypes(invTypes)
                    .sectors(sectors)
                    .verticals(verticals)
                    .macroAreas(macroAreas)
                    .type(fieldSet.readString("type"))
                    .macroType(fieldSet.readString("macroType"))
                    .website(fieldSet.readString("website"))
                    .image(fieldSet.readString("image"))
                    .isOld(fieldSet.readBoolean("isOld"))
                    .creatorEmail(fieldSet.readString("creatorEmail"))
                    .adminEmail(fieldSet.readString("adminEmail"))
                    .completenessScore(fieldSet.readInt("completenessScore"))
                    .impressions(fieldSet.readInt("impressions"))
                    .hqLocation(address)
                    .financials(fin)
                    .descriptions(desc)
                    .contacts(new HashSet<>(List.of(contact)))
                    .build();
        });

        reader.setLineMapper(lineMapper);
        return reader;
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

    @Bean
    @StepScope
    public JsonItemReader<InvestorDto> jsonReader(
            @Value("#{jobParameters['filePath']}") String filePath
    ) {
        return new JsonItemReaderBuilder<InvestorDto>()
                .name("investorJsonReader")
                .resource(new FileSystemResource(filePath))
                .jsonObjectReader(
                        new JacksonJsonObjectReader<>(new ObjectMapper(), InvestorDto.class))
                .build();
    }

    @Bean
    public ItemProcessor<InvestorDto, InvestorDto> processor() {
        return dto -> {
            // your validation logic…
            return dto;
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
}
