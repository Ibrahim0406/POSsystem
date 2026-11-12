package pl.com.possystem.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.possystem.domain.AccountStatus;
import pl.com.possystem.domain.StoreStatus;
import pl.com.possystem.domain.SupportReportStatus;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.payload.dto.*;
import pl.com.possystem.service.AdminService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/stores")
    public ResponseEntity<List<AdminStoreDTO>> getStores(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) StoreStatus status) {
        return ResponseEntity.ok(adminService.getStores(query, status));
    }

    @GetMapping("/stores/{id}")
    public ResponseEntity<AdminStoreDTO> getStore(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(adminService.getStore(id));
    }

    @PatchMapping("/stores/{id}/status")
    public ResponseEntity<AdminStoreDTO> updateStoreStatus(
            @PathVariable Long id, @RequestBody StoreStatusUpdateDTO request) throws Exception {
        return ResponseEntity.ok(adminService.updateStoreStatus(id, request.getStatus()));
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDTO>> getUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long branchId) {
        return ResponseEntity.ok(adminService.getUsers(query, role, status, storeId, branchId));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserDTO> getUser(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(adminService.getUser(id));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<AdminUserDTO> updateUserRole(
            @PathVariable Long id, @RequestBody AdminUserRoleUpdateDTO request) throws Exception {
        return ResponseEntity.ok(adminService.updateUserRole(id, request.getRole()));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<AdminUserDTO> updateUserStatus(
            @PathVariable Long id, @RequestBody AccountStatusUpdateDTO request) throws Exception {
        return ResponseEntity.ok(adminService.updateUserStatus(id, request.getStatus()));
    }

    @GetMapping("/support-reports")
    public ResponseEntity<List<SupportReportDTO>> getSupportReports(
            @RequestParam(required = false) SupportReportStatus status) {
        return ResponseEntity.ok(adminService.getSupportReports(status));
    }

    @PatchMapping("/support-reports/{id}/status")
    public ResponseEntity<SupportReportDTO> updateSupportReportStatus(
            @PathVariable Long id, @RequestBody SupportReportStatusDTO request) throws Exception {
        return ResponseEntity.ok(adminService.updateSupportReportStatus(id, request.getStatus()));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AdminAuditLogDTO>> getAuditLogs() {
        return ResponseEntity.ok(adminService.getAuditLogs());
    }

    @GetMapping("/settings")
    public ResponseEntity<List<SystemSettingDTO>> getSettings() {
        return ResponseEntity.ok(adminService.getSettings());
    }

    @PutMapping("/settings/{key}")
    public ResponseEntity<SystemSettingDTO> updateSetting(
            @PathVariable String key, @RequestBody SystemSettingUpdateDTO request) throws Exception {
        return ResponseEntity.ok(adminService.updateSetting(key, request.getValue()));
    }
}
