package pl.com.possystem.payload.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.com.possystem.entity.Store;

import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {


    private Long id;


    private String sku;


    private String name;


    private Double mrp;

    private String description;

    private Double sellingPrice;

    private String brand;

    private String imageUrl;

    private CategoryDTO category;

    private Long categoryId;

    private Long storeId;


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
