package pl.com.possystem.payload.dto;

import lombok.Data;
import pl.com.possystem.domain.AccountStatus;

@Data
public class AccountStatusUpdateDTO {
    private AccountStatus status;
}
