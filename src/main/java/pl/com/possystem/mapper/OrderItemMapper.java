package pl.com.possystem.mapper;

import pl.com.possystem.entity.OrderItem;
import pl.com.possystem.payload.dto.OrderItemDTO;

public class OrderItemMapper {

    public static OrderItemDTO toDTO(OrderItem orderItem){

        if(orderItem == null) return null;

        return OrderItemDTO.builder()
                .id(orderItem.getId())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .product(ProductMapper.toDTO(orderItem.getProduct()))
                .productId(orderItem.getProduct().getId())
                .orderId(orderItem.getOrder().getId())
                .build();
    }
}
