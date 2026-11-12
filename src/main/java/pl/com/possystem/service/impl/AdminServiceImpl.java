package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.possystem.domain.AccountStatus;
import pl.com.possystem.domain.StoreStatus;
import pl.com.possystem.domain.SupportReportStatus;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.entity.*;
import pl.com.possystem.exception.UserException;
import pl.com.possystem.payload.dto.*;
import pl.com.possystem.repository.*;
import pl.com.possystem.service.AdminService;
import pl.com.possystem.service.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final SupportReportRepository supportReportRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final SystemSettingRepository settingRepository;
    private final UserService userService;

    @Override
    public AdminDashboardDTO getDashboard() {
        Map<String, Long> storesByStatus = Arrays.stream(StoreStatus.values())
                .collect(Collectors.toMap(Enum::name, storeRepository::countByStatus));
        Map<String, Long> usersByRole = Arrays.stream(UserRole.values())
                .collect(Collectors.toMap(Enum::name, userRepository::countByRole));
        return AdminDashboardDTO.builder()
                .stores(storeRepository.count())
                .storesByStatus(storesByStatus)
                .users(userRepository.count())
                .usersByRole(usersByRole)
                .openSupportReports(supportReportRepository.countByStatus(SupportReportStatus.OPEN))
                .build();
    }

    @Override
    public List<AdminStoreDTO> getStores(String query, StoreStatus status) {
        List<Store> stores = query == null || query.isBlank()
                ? storeRepository.findAll()
                : storeRepository.findByBrandContainingIgnoreCase(query.trim());
        return stores.stream()
                .filter(store -> status == null || store.getStatus() == status)
                .map(this::toAdminStoreDTO)
                .toList();
    }

    @Override
    public AdminStoreDTO getStore(Long storeId) throws Exception {
        return toAdminStoreDTO(storeRepository.findById(storeId)
                .orElseThrow(() -> new Exception("Store not found")));
    }

    @Override
    @Transactional
    public AdminStoreDTO updateStoreStatus(Long storeId, StoreStatus status) throws Exception {
        if (status == null) {
            throw new UserException("Store status is required");
        }
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new Exception("Store not found"));
        StoreStatus previousStatus = store.getStatus();
        store.setStatus(status);
        Store updated = storeRepository.save(store);
        audit("STORE_STATUS_UPDATED", "STORE", storeId.toString(),
                "status: " + previousStatus + " -> " + status);
        return toAdminStoreDTO(updated);
    }

    @Override
    public List<AdminUserDTO> getUsers(
            String query, UserRole role, AccountStatus status, Long storeId, Long branchId) {
        String normalizedQuery = query == null ? null : query.trim().toLowerCase(Locale.ROOT);
        return userRepository.findAll().stream()
                .filter(user -> normalizedQuery == null || normalizedQuery.isBlank()
                        || containsIgnoreCase(user.getFullName(), normalizedQuery)
                        || containsIgnoreCase(user.getEmail(), normalizedQuery))
                .filter(user -> role == null || user.getRole() == role)
                .filter(user -> status == null || effectiveStatus(user) == status)
                .filter(user -> storeId == null || Objects.equals(getStoreId(user), storeId))
                .filter(user -> branchId == null || user.getBranch() != null
                        && Objects.equals(user.getBranch().getId(), branchId))
                .map(this::toAdminUserDTO)
                .toList();
    }

    @Override
    public AdminUserDTO getUser(Long userId) throws Exception {
        return toAdminUserDTO(userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found")));
    }

    @Override
    @Transactional
    public AdminUserDTO updateUserRole(Long userId, UserRole role) throws Exception {
        if (role == null || role == UserRole.ROLE_ADMIN) {
            throw new UserException("The system administrator role cannot be assigned through this endpoint");
        }
        User actor = userService.getCurrentUser();
        if (Objects.equals(actor.getId(), userId)) {
            throw new UserException("You cannot change your own role");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            throw new UserException("System administrator accounts cannot be changed here");
        }
        UserRole previousRole = user.getRole();
        user.setRole(role);
        User updated = userRepository.save(user);
        audit("USER_ROLE_UPDATED", "USER", userId.toString(),
                "role: " + previousRole + " -> " + role);
        return toAdminUserDTO(updated);
    }

    @Override
    @Transactional
    public AdminUserDTO updateUserStatus(Long userId, AccountStatus status) throws Exception {
        if (status == null) {
            throw new UserException("Account status is required");
        }
        User actor = userService.getCurrentUser();
        if (status == AccountStatus.DISABLED && Objects.equals(actor.getId(), userId)) {
            throw new UserException("You cannot disable your own account");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            throw new UserException("System administrator accounts cannot be disabled here");
        }
        AccountStatus previousStatus = effectiveStatus(user);
        user.setStatus(status);
        User updated = userRepository.save(user);
        audit("USER_STATUS_UPDATED", "USER", userId.toString(),
                "status: " + previousStatus + " -> " + status);
        return toAdminUserDTO(updated);
    }

    @Override
    @Transactional
    public SupportReportDTO createSupportReport(SupportReportCreateDTO request) throws Exception {
        if (request.getTitle() == null || request.getTitle().isBlank()
                || request.getTitle().length() > 200) {
            throw new UserException("Title is required and must be at most 200 characters");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()
                || request.getDescription().length() > 5000) {
            throw new UserException("Description is required and must be at most 5000 characters");
        }
        SupportReport report = SupportReport.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .submittedBy(userService.getCurrentUser())
                .status(SupportReportStatus.OPEN)
                .build();
        return toSupportReportDTO(supportReportRepository.save(report));
    }

    @Override
    public List<SupportReportDTO> getSupportReports(SupportReportStatus status) {
        return supportReportRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(report -> status == null || report.getStatus() == status)
                .map(this::toSupportReportDTO)
                .toList();
    }

    @Override
    @Transactional
    public SupportReportDTO updateSupportReportStatus(Long reportId, SupportReportStatus status) throws Exception {
        if (status == null) {
            throw new UserException("Report status is required");
        }
        SupportReport report = supportReportRepository.findById(reportId)
                .orElseThrow(() -> new Exception("Support report not found"));
        SupportReportStatus previousStatus = report.getStatus();
        report.setStatus(status);
        SupportReport updated = supportReportRepository.save(report);
        audit("SUPPORT_REPORT_STATUS_UPDATED", "SUPPORT_REPORT", reportId.toString(),
                "status: " + previousStatus + " -> " + status);
        return toSupportReportDTO(updated);
    }

    @Override
    public List<AdminAuditLogDTO> getAuditLogs() {
        return auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(log -> AdminAuditLogDTO.builder()
                        .id(log.getId())
                        .actorId(log.getActorId())
                        .actorEmail(log.getActorEmail())
                        .action(log.getAction())
                        .resourceType(log.getResourceType())
                        .resourceId(log.getResourceId())
                        .details(log.getDetails())
                        .createdAt(log.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public List<SystemSettingDTO> getSettings() {
        return settingRepository.findAll(Sort.by(Sort.Direction.ASC, "settingKey")).stream()
                .map(this::toSystemSettingDTO)
                .toList();
    }

    @Override
    @Transactional
    public SystemSettingDTO updateSetting(String key, String value) throws Exception {
        if (key == null || !key.matches("[A-Za-z0-9._-]{1,100}")) {
            throw new UserException("Setting key must contain only letters, numbers, '.', '_' or '-'");
        }
        if (value == null || value.length() > 4000) {
            throw new UserException("Setting value is required and must be at most 4000 characters");
        }
        SystemSetting setting = settingRepository.findById(key)
                .orElseGet(() -> SystemSetting.builder().settingKey(key).build());
        setting.setSettingValue(value);
        SystemSetting updated = settingRepository.save(setting);
        audit("SYSTEM_SETTING_UPDATED", "SYSTEM_SETTING", key, "Setting value updated");
        return toSystemSettingDTO(updated);
    }

    private void audit(String action, String resourceType, String resourceId, String details)
            throws UserException {
        User actor = userService.getCurrentUser();
        auditLogRepository.save(AdminAuditLog.builder()
                .actorId(actor.getId())
                .actorEmail(actor.getEmail())
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(details)
                .build());
    }

    private AdminStoreDTO toAdminStoreDTO(Store store) {
        return AdminStoreDTO.builder()
                .id(store.getId())
                .name(store.getBrand())
                .status(store.getStatus())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .contact(store.getContact())
                .storeAdminId(store.getStoreAdmin() == null ? null : store.getStoreAdmin().getId())
                .storeAdminEmail(store.getStoreAdmin() == null ? null : store.getStoreAdmin().getEmail())
                .build();
    }

    private AdminUserDTO toAdminUserDTO(User user) {
        return AdminUserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(effectiveStatus(user))
                .storeId(getStoreId(user))
                .branchId(user.getBranch() == null ? null : user.getBranch().getId())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    private SupportReportDTO toSupportReportDTO(SupportReport report) {
        return SupportReportDTO.builder()
                .id(report.getId())
                .title(report.getTitle())
                .description(report.getDescription())
                .status(report.getStatus())
                .submittedById(report.getSubmittedBy() == null ? null : report.getSubmittedBy().getId())
                .submittedByEmail(report.getSubmittedBy() == null ? null : report.getSubmittedBy().getEmail())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }

    private SystemSettingDTO toSystemSettingDTO(SystemSetting setting) {
        return SystemSettingDTO.builder()
                .key(setting.getSettingKey())
                .value(setting.getSettingValue())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }

    private AccountStatus effectiveStatus(User user) {
        return user.getStatus() == AccountStatus.DISABLED
                ? AccountStatus.DISABLED : AccountStatus.ACTIVE;
    }

    private Long getStoreId(User user) {
        if (user.getStore() != null) {
            return user.getStore().getId();
        }
        return user.getBranch() == null || user.getBranch().getStore() == null
                ? null : user.getBranch().getStore().getId();
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
