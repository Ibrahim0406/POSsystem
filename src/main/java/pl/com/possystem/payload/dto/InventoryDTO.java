package pl.com.possystem.payload.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.com.possystem.entity.Branch;
import pl.com.possystem.entity.Product;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {


    private Long id;

    private BranchDTO branch;

    private ProductDTO product;

    private Long branchId;

    private Long productId;

    private Integer quantity;

    private LocalDateTime lastUpdated;
}
