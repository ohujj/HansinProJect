package com.withgpt.gpt.controller;

import com.withgpt.gpt.model.Post;
import com.withgpt.gpt.service.FileService;
import com.withgpt.gpt.service.ImageGenerationService;
import com.withgpt.gpt.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/uploads")
public class FileController {

    private final Logger logger = LoggerFactory.getLogger(FileController.class);
    private final Path fileStorageLocation = Paths.get("C:/uploads").toAbsolutePath().normalize();
    private final ImageGenerationService imageGenerationService; // AI 이미지 생성 서비스 추가
    private final FileService fileService; // 파일 서비스 추가

    private final PostService postService;

    public FileController(ImageGenerationService imageGenerationService, FileService fileService, PostService postService) {
        this.imageGenerationService = imageGenerationService;
        this.fileService = fileService;
        this.postService = postService;
    }

    // 파일 업로드 처리
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        try {
            Files.createDirectories(fileStorageLocation);
            String fileName = file.getOriginalFilename();
            Path targetLocation = fileStorageLocation.resolve(fileName);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            logger.info("파일 저장됨: {}", targetLocation.toString());

            model.addAttribute("uploadedFile", fileName);  // 첨부파일로 표시
            return "redirect:/uploads/" + fileName;
        } catch (Exception ex) {  // IOException 예외를 일반 Exception으로 처리
            logger.error("파일 저장 중 오류 발생", ex);
            return "error";
        }
    }

    @PostMapping("/generateAI")
    public ResponseEntity<Map<String, Object>> generateAIImage(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String aiImageUrl = request.get("imageUrl"); // 클라이언트에서 전달된 AI 이미지 URL

        logger.info("클라이언트로부터 전달된 AI 이미지 URL: {}", aiImageUrl);  // 전달된 이미지 URL 로그

        try {
            // 1. 이미지 URL 확인
            if (aiImageUrl == null || aiImageUrl.isEmpty()) {
                response.put("status", "error");
                response.put("message", "이미지 URL이 없습니다.");
                logger.warn("이미지 URL이 제공되지 않았습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 2. 이미지 서버에 저장 (파일 이름 생성 및 저장)
            logger.info("저장 전 AI 이미지 URL: {}", aiImageUrl);
            String savedFileName = fileService.saveAIImageFromUrl(aiImageUrl);

            // 3. 이미지 저장 경로 확인
            if (savedFileName == null || savedFileName.isEmpty()) {
                response.put("status", "error");
                response.put("message", "이미지 저장에 실패했습니다.");
                logger.error("이미지 파일 저장 실패: URL: {}", aiImageUrl);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // 4. 성공 응답: 저장된 파일 이름만 클라이언트로 전달
            response.put("status", "success");
            response.put("message", "섬네일로 등록이 되었습니다.");
            response.put("fileName", savedFileName); // 경로 없이 파일 이름만 전달

            logger.info("저장된 AI 이미지 파일명: {}", savedFileName);  // 저장된 파일명 로그

            return ResponseEntity.ok(response); // 200 OK와 함께 응답 반환

        } catch (Exception e) {
            logger.error("AI 이미지 저장 중 예기치 않은 오류 발생", e);

            // 5. 기타 예기치 않은 오류 응답
            response.put("status", "error");
            response.put("message", "AI 이미지 저장 중 예기치 않은 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // 파일 요청 처리
    @GetMapping("/{fileName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String fileName) {
        try {
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            logger.info("파일 요청 경로: {}", filePath.toString());

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .body(resource);
            } else {
                logger.error("파일을 찾을 수 없음: {}", fileName);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            logger.error("잘못된 파일 경로", ex);
            return ResponseEntity.badRequest().build();
        }
    }

    // 파일 삭제 처리
    @DeleteMapping("/{fileName:.+}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
        try {
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            logger.info("파일 삭제됨: {}", filePath.toString());

            return ResponseEntity.ok("파일이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            logger.error("파일 삭제 중 오류 발생: {}", fileName, e);
            return ResponseEntity.status(500).body("파일 삭제 중 오류가 발생했습니다.");
        }
    }


}
