package pl.com.possystem.service;

import pl.com.possystem.domain.AccountStatus;
import pl.com.possystem.domain.StoreStatus;
import pl.com.possystem.domain.SupportReportStatus;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.payload.dto.*;

import java.util.List;

public interface AdminService {
    AdminDashboardDTO getDashboard();
    List<AdminStoreDTO> getStores(String query, StoreStatus status);
    AdminStoreDTO getStore(Long storeId) throws Exception;
    AdminStoreDTO updateStoreStatus(Long storeId, StoreStatus status) throws Exception;
    List<AdminUserDTO> getUsers(String query, UserRole role, AccountStatus status, Long storeId, Long branchId);
    AdminUserDTO getUser(Long userId) throws Exception;
    AdminUserDTO updateUserRole(Long userId, UserRole role) throws Exception;
    AdminUserDTO updateUserStatus(Long userId, AccountStatus status) throws Exception;
    SupportReportDTO createSupportReport(SupportReportCreateDTO request) throws Exception;
    List<SupportReportDTO> getSupportReports(SupportReportStatus status);
    SupportReportDTO updateSupportReportStatus(Long reportId, SupportReportStatus status) throws Exception;
    List<AdminAuditLogDTO> getAuditLogs();
    List<SystemSettingDTO> getSettings();
    SystemSettingDTO updateSetting(String key, String value) throws Exception;
}
