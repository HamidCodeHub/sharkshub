package com.ucapital.sharkshub.investor.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;


@Service
public class LocalFileStorageService implements FileStorageService {

    @Override
    public String saveToTemp(MultipartFile file) throws IOException {
        // create a temp file in the default temp directory
        String ext = Optional.ofNullable(file.getOriginalFilename())
                .filter(n -> n.contains("."))
                .map(n -> n.substring(n.lastIndexOf('.')))
                .orElse("");
        Path tempFile = Files.createTempFile("investor-import-", ext);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile.toAbsolutePath().toString();
    }

    @Override
    public String checksum(String filePath) throws IOException {
        try (InputStream fis = new FileInputStream(filePath)) {
            // SHA-256 hex
            return DigestUtils.sha256Hex(fis);
        }
    }
}