package pl.com.possystem.payload.dto;

import lombok.Builder;
import lombok.Data;
import pl.com.possystem.domain.StoreStatus;
import pl.com.possystem.entity.StoreContact;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminStoreDTO {
    private Long id;
    private String name;
    private StoreStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private StoreContact contact;
    private Long storeAdminId;
    private String storeAdminEmail;
}
