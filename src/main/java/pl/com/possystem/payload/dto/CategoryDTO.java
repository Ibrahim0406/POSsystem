package pl.com.possystem.payload.dto;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import pl.com.possystem.entity.Store;

@Data
@Builder
public class CategoryDTO {

    private Long id;

    private String name;

   // private Store store;

    private Long storeId;
}
