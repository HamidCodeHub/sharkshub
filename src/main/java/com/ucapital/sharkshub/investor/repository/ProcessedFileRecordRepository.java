package com.ucapital.sharkshub.investor.repository;

import com.ucapital.sharkshub.investor.model.ProcessedFileRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessedFileRecordRepository
        extends MongoRepository<ProcessedFileRecord, String> {

     Optional<ProcessedFileRecord> findByChecksum(String checksum);
     boolean existsByChecksum(String checksum);
}