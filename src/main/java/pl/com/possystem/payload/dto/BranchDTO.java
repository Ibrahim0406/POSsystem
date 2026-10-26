package pl.com.possystem.payload.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.com.possystem.entity.Store;
import pl.com.possystem.entity.User;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BranchDTO {


    private Long id;

    private String name;

    private String address;

    private String phoneNumber;

    private String email;

    private List<String> workingDays;

    private LocalTime openTime;

    private LocalTime closeTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private StoreDto store;

    private Long storeId;

    private UserDto manager;
}
