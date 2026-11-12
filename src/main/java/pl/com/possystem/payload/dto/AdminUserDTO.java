package pl.com.possystem.payload.dto;

import lombok.Builder;
import lombok.Data;
import pl.com.possystem.domain.AccountStatus;
import pl.com.possystem.domain.UserRole;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserDTO {
    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private AccountStatus status;
    private Long storeId;
    private Long branchId;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
