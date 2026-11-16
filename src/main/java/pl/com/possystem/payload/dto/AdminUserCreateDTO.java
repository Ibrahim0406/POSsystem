package pl.com.possystem.payload.dto;

import lombok.Data;
import pl.com.possystem.domain.AccountStatus;
import pl.com.possystem.domain.UserRole;

@Data
public class AdminUserCreateDTO {
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private UserRole role;
    private AccountStatus status;
    private Long storeId;
    private Long branchId;
}
