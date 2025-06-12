package com.ucapital.sharkshub.investor.service;

import com.ucapital.sharkshub.investor.dto.BulkOperationResponse;
import com.ucapital.sharkshub.investor.dto.InvestorDto;
import com.ucapital.sharkshub.investor.model.Investor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface InvestorService {
    @Transactional
    BulkOperationResponse bulkInsert(List<InvestorDto> investorDtos);

    @Transactional
    BulkOperationResponse bulkInsertFromFile(MultipartFile file) throws IOException;

    public BulkOperationResponse getBulkJobStatus(long jobExecutionId);

    public long launchBulkInsertJob(MultipartFile file) throws IOException;

    Optional<InvestorDto> findById(String id);

    Optional<InvestorDto> findByName(String name);

    List<InvestorDto> findAll();

    Investor convertToEntity(InvestorDto dto);

    InvestorDto convertToDto(Investor entity);


}
