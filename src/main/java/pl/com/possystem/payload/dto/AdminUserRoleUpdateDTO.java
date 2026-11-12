package pl.com.possystem.payload.dto;

import lombok.Data;
import pl.com.possystem.domain.UserRole;

@Data
public class AdminUserRoleUpdateDTO {
    private UserRole role;
}
