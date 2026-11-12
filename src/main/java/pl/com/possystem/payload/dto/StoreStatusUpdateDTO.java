package pl.com.possystem.payload.dto;

import lombok.Data;
import pl.com.possystem.domain.StoreStatus;

@Data
public class StoreStatusUpdateDTO {
    private StoreStatus status;
}
