package pl.com.possystem.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.possystem.payload.dto.SupportReportCreateDTO;
import pl.com.possystem.payload.dto.SupportReportDTO;
import pl.com.possystem.service.AdminService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/support/reports")
public class SupportReportController {

    private final AdminService adminService;

    @PostMapping
    public ResponseEntity<SupportReportDTO> createReport(
            @RequestBody SupportReportCreateDTO request) throws Exception {
        return ResponseEntity.ok(adminService.createSupportReport(request));
    }
}
