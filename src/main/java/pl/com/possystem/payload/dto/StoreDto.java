package pl.com.possystem.payload.dto;

import jakarta.persistence.*;
import lombok.Data;
import pl.com.possystem.domain.StoreStatus;
import pl.com.possystem.entity.StoreContact;
import pl.com.possystem.entity.User;

import java.time.LocalDateTime;

@Data
public class StoreDto {


    private Long id;

    private String brand;

    private UserDto storeAdmin;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String description;

    private String storeType;

    private StoreStatus status;

    private StoreContact contact;
}
