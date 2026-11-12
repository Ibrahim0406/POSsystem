package pl.com.possystem.payload.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.com.possystem.entity.Order;
import pl.com.possystem.entity.Product;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {


    private Long id;

    private Integer quantity;

    private Double price;

    private ProductDTO product;

    private Long productId;

    private Long orderId;

}
