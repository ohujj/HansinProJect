package com.withgpt.gpt.service;

import com.withgpt.gpt.controller.FileController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileService {
    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final Path fileStorageLocation;

    public FileService() {
        // 파일 저장 경로 설정 (외부 경로로 설정)
        this.fileStorageLocation = Paths.get("C:/uploads").toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("파일 저장 디렉토리를 생성할 수 없습니다.", ex);
        }
    }

    // 기존 파일 저장 로직
    public String saveFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String newFileName = UUID.randomUUID().toString() + "_" + originalFilename;  // 유니크한 파일명 생성
        Path targetLocation = this.fileStorageLocation.resolve(newFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return newFileName;  // 파일명만 반환
    }

    // AI 이미지 URL에서 이미지 저장 로직
    public String saveAIImageFromUrl(String imageUrl) {
        try (InputStream in = new URL(imageUrl).openStream()) {
            String fileName = UUID.randomUUID().toString() + ".png";  // 고유한 파일 이름 생성
            Path uploadDir = Paths.get("C:/uploads/");  // 저장 경로 설정

            // 경로가 없을 경우 디렉토리 생성
            if (Files.notExists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path imagePath = uploadDir.resolve(fileName);  // 저장할 파일 경로 생성
            logger.info("이미지를 저장할 경로: {}", imagePath);  // 이미지 저장 경로 로그

            Files.copy(in, imagePath, StandardCopyOption.REPLACE_EXISTING);  // 이미지 파일 저장

            logger.info("이미지 저장 성공: {}", imagePath.toString());  // 이미지 저장 성공 로그

            // 서버에서 접근 가능한 URL 반환 (예: /uploads/{fileName})
            return fileName;
        } catch (IOException e) {
            logger.error("AI 이미지 저장 실패", e);
            throw new RuntimeException("AI 이미지 저장 실패", e);
        }
    }


    // 파일 불러오기 로직
    public Resource loadFileAsResource(String fileName) throws MalformedURLException {
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists()) {
            return resource;
        } else {
            throw new RuntimeException("파일을 찾을 수 없습니다. " + fileName);
        }
    }

    // 파일 삭제 로직 (선택적 추가 기능)
    public boolean deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            return true;  // 성공적으로 삭제됨
        } catch (IOException e) {
            return false;  // 삭제 실패
        }
    }
}
