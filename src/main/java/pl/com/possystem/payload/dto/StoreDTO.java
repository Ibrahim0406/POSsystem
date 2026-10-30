package pl.com.possystem.payload.dto;

import lombok.Data;
import pl.com.possystem.domain.StoreStatus;
import pl.com.possystem.entity.StoreContact;

import java.time.LocalDateTime;

@Data
public class StoreDTO {


    private Long id;

    private String brand;

    private UserDTO storeAdmin;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String description;

    private String storeType;

    private StoreStatus status;

    private StoreContact contact;
}
