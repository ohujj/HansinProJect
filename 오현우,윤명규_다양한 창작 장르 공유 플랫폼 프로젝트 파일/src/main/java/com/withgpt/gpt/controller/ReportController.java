package com.withgpt.gpt.controller;

import com.withgpt.gpt.model.Report;
import com.withgpt.gpt.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<Report> createReport(@RequestBody Report report) {
        // 수신된 데이터 로깅
        System.out.printf("Received report: %s%n", report);

        // 필수 값 검증
        if (isInvalidField(report.getReportedAuthor(), "Reported author")) {
            return badRequestResponse("Reported author is required.");
        }
        if (isInvalidField(report.getReportedCategory(), "Reported category")) {
            return badRequestResponse("Reported category is required.");
        }

        // Post 객체의 ID 검증
        if (report.getPost() == null || report.getPost().getId() == null) {
            System.out.println("Post ID is missing");
            return badRequestResponse("Post ID is required.");
        }

        // 신고 데이터 저장
        try {
            Report savedReport = reportService.createReport(report);
            System.out.printf("Report successfully saved: %s%n", savedReport);
            return new ResponseEntity<>(savedReport, HttpStatus.CREATED);  // 201 상태 반환
        } catch (Exception e) {
            System.err.printf("Error saving report: %s%n", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);  // 500 상태 반환
        }
    }


    @GetMapping
    public List<Report> getAllReports() {
        return reportService.getAllReports();
    }

    // 필드 유효성 검증 함수
    private boolean isInvalidField(String field, String fieldName) {
        if (Objects.isNull(field) || field.trim().isEmpty()) {
            System.out.printf("%s is invalid (null or empty)%n", fieldName);
            return true;
        }
        return false;
    }

    // 잘못된 요청 응답 생성 함수
    private ResponseEntity<Report> badRequestResponse(String message) {
        System.out.println("Bad request: " + message);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
