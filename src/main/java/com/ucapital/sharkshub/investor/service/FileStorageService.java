package com.ucapital.sharkshub.investor.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {

    String saveToTemp(MultipartFile file) throws IOException;

    String checksum(String filePath) throws IOException;
}
