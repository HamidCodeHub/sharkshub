package com.ucapital.sharkshub.investor.service;

import com.ucapital.sharkshub.investor.dto.*;
import com.ucapital.sharkshub.investor.exception.InvestorValidationException;
import com.ucapital.sharkshub.investor.model.Investor;
import com.ucapital.sharkshub.investor.repository.InvestorRepository;
import com.ucapital.sharkshub.investor.util.BulkInsertUtil;
import com.ucapital.sharkshub.investor.util.FileParser;
import com.ucapital.sharkshub.investor.validation.InvestorValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class InvestorServiceImpl implements InvestorService {

    private static final Logger logger = LoggerFactory.getLogger(InvestorServiceImpl.class);

    private final InvestorRepository investorRepository;
    private final InvestorValidator investorValidator;
    private final FileParser fileParser;
    private final BulkInsertUtil bulkInsertUtil;

    @Autowired
    public InvestorServiceImpl(
            InvestorRepository investorRepository,
            InvestorValidator investorValidator,
            FileParser fileParser,
            BulkInsertUtil bulkInsertUtil) {
        this.investorRepository = investorRepository;
        this.investorValidator = investorValidator;
        this.fileParser = fileParser;
        this.bulkInsertUtil = bulkInsertUtil;
    }


    @Transactional
    @Override
    public BulkOperationResponse bulkInsert(List<InvestorDto> investorDtos) {
        if (investorDtos == null || investorDtos.isEmpty()) {
            return BulkOperationResponse.builder()
                    .totalProcessed(0)
                    .successCount(0)
                    .failureCount(0)
                    .status(OperationStatus.COMPLETED)
                    .message("No investors to process")
                    .build();
        }

        logger.info("Starting bulk insert of {} investors", investorDtos.size());

        BulkOperationResponse response = BulkOperationResponse.builder()
                .totalProcessed(investorDtos.size())
                .build();

        List<Investor> validInvestors = new ArrayList<>();

        for (int i = 0; i < investorDtos.size(); i++) {
            try {
                InvestorDto dto = investorDtos.get(i);

                investorValidator.validateInvestor(dto, i, true);

                Investor investor = convertToEntity(dto);

                if (investor.getCreatedAt() == null) {
                    investor.setCreatedAt(Instant.now());
                }
                investor.setUpdatedAt(Instant.now());

                validInvestors.add(investor);
            } catch (InvestorValidationException e) {
                logger.warn("Validation failed for investor at index {}: {}", i, e.getMessage());

                if (e.hasValidationErrors()) {
                    for (InvestorValidationException.ValidationError error : e.getValidationErrors()) {
                        response.addValidationError(
                                i,
                                e.getInvestorName(),
                                error.getFieldName(),
                                error.getRejectedValue(),
                                error.getErrorMessage());
                    }
                } else {
                    response.addError(i, e.getInvestorName(), "VALIDATION_ERROR", e.getMessage());
                }
            } catch (Exception e) {
                logger.error("Error processing investor at index {}: {}", i, e.getMessage(), e);
                response.addError(i, investorDtos.get(i).getName(), "PROCESSING_ERROR", e.getMessage());
            }
        }

        if (!validInvestors.isEmpty()) {
            try {
                BulkOperationResponse insertResponse = bulkInsertUtil.bulkInsert(validInvestors);

                response.setSuccessCount(insertResponse.getSuccessCount());
                response.setFailureCount(response.getTotalProcessed() - response.getSuccessCount());

                insertResponse.getErrors().forEach(error ->
                        response.addError(error.getItemIndex(), error.getInvestorName(),
                                error.getErrorCode(), error.getErrorMessage()));

                insertResponse.getWarnings().forEach(response::addWarning);
            } catch (Exception e) {
                logger.error("Error during bulk insert: {}", e.getMessage(), e);

                response.setSuccessCount(0);
                response.setFailureCount(response.getTotalProcessed());
                response.addError(0, null, "BULK_INSERT_ERROR", e.getMessage());
            }
        }

        response.updateStatus();
        logger.info("Bulk insert completed. Total: {}, Success: {}, Failed: {}",
                response.getTotalProcessed(), response.getSuccessCount(), response.getFailureCount());

        return response;
    }


    @Transactional
    @Override
    public BulkOperationResponse bulkInsertFromFile(MultipartFile file) throws IOException {
        logger.info("Starting bulk insert from file: {}", file.getOriginalFilename());

        try {
            List<InvestorDto> investorDtos = fileParser.parseFile(file);

            return bulkInsert(investorDtos);
        } catch (InvestorValidationException e) {
            logger.error("Validation error during file parsing: {}", e.getMessage(), e);

            BulkOperationResponse response = BulkOperationResponse.builder()
                    .totalProcessed(1)
                    .successCount(0)
                    .failureCount(1)
                    .status(OperationStatus.FAILED)
                    .message("File validation failed: " + e.getMessage())
                    .build();

            response.addError(0, file.getOriginalFilename(), "FILE_VALIDATION_ERROR", e.getMessage());
            return response;
        } catch (IOException e) {
            logger.error("IO error during file parsing: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during file parsing: {}", e.getMessage(), e);

            BulkOperationResponse response = BulkOperationResponse.builder()
                    .totalProcessed(1)
                    .successCount(0)
                    .failureCount(1)
                    .status(OperationStatus.FAILED)
                    .message("File processing failed: " + e.getMessage())
                    .build();

            response.addError(0, file.getOriginalFilename(), "FILE_PROCESSING_ERROR", e.getMessage());
            return response;
        }
    }


    @Override
    public Optional<InvestorDto> findById(String id) {
        return investorRepository.findById(id)
                .map(this::convertToDto);
    }


    @Override
    public Optional<InvestorDto> findByName(String name) {
        return investorRepository.findByName(name)
                .map(this::convertToDto);
    }


    @Override
    public List<InvestorDto> findAll() {
        return investorRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    @Override
    public Investor convertToEntity(InvestorDto dto) {
        if (dto == null) {
            return null;
        }

        Investor investor = new Investor();

        investor.setId(dto.getId());
        investor.setName(dto.getName());
        investor.setStatus(dto.getStatus());
        investor.setType(dto.getType());
        investor.setMacroType(dto.getMacroType());
        investor.setWebsite(dto.getWebsite());
        investor.setImage(dto.getImage());
        investor.setIsOld(dto.getIsOld());
        investor.setCreatorEmail(dto.getCreatorEmail());
        investor.setAdminEmail(dto.getAdminEmail());
        investor.setCompletenessScore(dto.getCompletenessScore());
        investor.setImpressions(dto.getImpressions());

        if (dto.getPreferredGeographicalAreas() != null) {
            investor.setPreferredGeographicalAreas(new ArrayList<>(dto.getPreferredGeographicalAreas()));
        }

        if (dto.getPreferredInvestmentTypes() != null) {
            investor.setPreferredInvestmentTypes(new ArrayList<>(dto.getPreferredInvestmentTypes()));
        }

        if (dto.getSectors() != null) {
            investor.setSectors(new ArrayList<>(dto.getSectors()));
        }

        if (dto.getVerticals() != null) {
            investor.setVerticals(new ArrayList<>(dto.getVerticals()));
        }

        if (dto.getMacroAreas() != null) {
            investor.setMacroAreas(new ArrayList<>(dto.getMacroAreas()));
        }

        investor.setHqLocation(convertAddressToEntity(dto.getHqLocation()));
        investor.setFinancials(convertFinancialsToEntity(dto.getFinancials()));
        investor.setDescriptions(convertDescriptionsToEntity(dto.getDescriptions()));
        investor.setContacts(convertContactsToEntitySet(dto.getContacts()));

        return investor;
    }


    private Investor.Address convertAddressToEntity(AddressDto dto) {
        if (dto == null) {
            return null;
        }

        return Investor.Address.builder()
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .zip(dto.getZip())
                .country(dto.getCountry())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .fax(dto.getFax())
                .sn(dto.getSn())
                .build();
    }


    private Investor.Financials convertFinancialsToEntity(FinancialsDto dto) {
        if (dto == null) {
            return null;
        }

        return Investor.Financials.builder()
                .invMin(dto.getInvMin())
                .invMax(dto.getInvMax())
                .invAvg(dto.getInvAvg())
                .dealMax(dto.getDealMax())
                .dealMin(dto.getDealMin())
                .cmpValMin(dto.getCmpValMin())
                .cmpValMax(dto.getCmpValMax())
                .ebitdaMin(dto.getEbitdaMin())
                .ebitdaMax(dto.getEbitdaMax())
                .ebitMin(dto.getEbitMin())
                .ebitMax(dto.getEbitMax())
                .build();
    }


    private Investor.InvDescriptions convertDescriptionsToEntity(InvDescriptionsDto dto) {
        if (dto == null) {
            return null;
        }

        return Investor.InvDescriptions.builder()
                .it(dto.getIt())
                .en(dto.getEn())
                .fr(dto.getFr())
                .de(dto.getDe())
                .es(dto.getEs())
                .ru(dto.getRu())
                .ch(dto.getCh())
                .build();
    }


    private Set<Investor.Contacts> convertContactsToEntitySet(Set<ContactsDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return new HashSet<>();
        }

        return dtos.stream()
                .map(dto -> Investor.Contacts.builder()
                        .firstName(dto.getFirstName())
                        .lastName(dto.getLastName())
                        .email(dto.getEmail())
                        .phone(dto.getPhone())
                        .mobile(dto.getMobile())
                        .fax(dto.getFax())
                        .role(dto.getRole())
                        .orderNum(dto.getOrderNum())
                        .build())
                .collect(Collectors.toSet());
    }


    @Override
    public InvestorDto convertToDto(Investor entity) {
        if (entity == null) {
            return null;
        }

        InvestorDto dto = new InvestorDto();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setStatus(entity.getStatus());
        dto.setType(entity.getType());
        dto.setMacroType(entity.getMacroType());
        dto.setWebsite(entity.getWebsite());
        dto.setImage(entity.getImage());
        dto.setIsOld(entity.getIsOld());
        dto.setCreatorEmail(entity.getCreatorEmail());
        dto.setAdminEmail(entity.getAdminEmail());
        dto.setCompletenessScore(entity.getCompletenessScore());
        dto.setImpressions(entity.getImpressions());

        if (entity.getPreferredGeographicalAreas() != null) {
            dto.setPreferredGeographicalAreas(new ArrayList<>(entity.getPreferredGeographicalAreas()));
        }

        if (entity.getPreferredInvestmentTypes() != null) {
            dto.setPreferredInvestmentTypes(new ArrayList<>(entity.getPreferredInvestmentTypes()));
        }

        if (entity.getSectors() != null) {
            dto.setSectors(new ArrayList<>(entity.getSectors()));
        }

        if (entity.getVerticals() != null) {
            dto.setVerticals(new ArrayList<>(entity.getVerticals()));
        }

        if (entity.getMacroAreas() != null) {
            dto.setMacroAreas(new ArrayList<>(entity.getMacroAreas()));
        }

        dto.setHqLocation(convertAddressToDto(entity.getHqLocation()));
        dto.setFinancials(convertFinancialsToDto(entity.getFinancials()));
        dto.setDescriptions(convertDescriptionsToDto(entity.getDescriptions()));
        dto.setContacts(convertContactsToDtoSet(entity.getContacts()));

        return dto;
    }


    private AddressDto convertAddressToDto(Investor.Address entity) {
        if (entity == null) {
            return null;
        }

        return AddressDto.builder()
                .address(entity.getAddress())
                .city(entity.getCity())
                .state(entity.getState())
                .zip(entity.getZip())
                .country(entity.getCountry())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .fax(entity.getFax())
                .sn(entity.getSn())
                .build();
    }


    private FinancialsDto convertFinancialsToDto(Investor.Financials entity) {
        if (entity == null) {
            return null;
        }

        return FinancialsDto.builder()
                .invMin(entity.getInvMin())
                .invMax(entity.getInvMax())
                .invAvg(entity.getInvAvg())
                .dealMax(entity.getDealMax())
                .dealMin(entity.getDealMin())
                .cmpValMin(entity.getCmpValMin())
                .cmpValMax(entity.getCmpValMax())
                .ebitdaMin(entity.getEbitdaMin())
                .ebitdaMax(entity.getEbitdaMax())
                .ebitMin(entity.getEbitMin())
                .ebitMax(entity.getEbitMax())
                .build();
    }


    private InvDescriptionsDto convertDescriptionsToDto(Investor.InvDescriptions entity) {
        if (entity == null) {
            return null;
        }

        return InvDescriptionsDto.builder()
                .it(entity.getIt())
                .en(entity.getEn())
                .fr(entity.getFr())
                .de(entity.getDe())
                .es(entity.getEs())
                .ru(entity.getRu())
                .ch(entity.getCh())
                .build();
    }


    private Set<ContactsDto> convertContactsToDtoSet(Set<Investor.Contacts> entities) {
        if (entities == null || entities.isEmpty()) {
            return new HashSet<>();
        }

        return entities.stream()
                .map(entity -> ContactsDto.builder()
                        .firstName(entity.getFirstName())
                        .lastName(entity.getLastName())
                        .email(entity.getEmail())
                        .phone(entity.getPhone())
                        .mobile(entity.getMobile())
                        .fax(entity.getFax())
                        .role(entity.getRole())
                        .orderNum(entity.getOrderNum())
                        .build())
                .collect(Collectors.toSet());
    }
}