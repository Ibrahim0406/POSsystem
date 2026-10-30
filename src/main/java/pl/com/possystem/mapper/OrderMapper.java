package pl.com.possystem.mapper;

import pl.com.possystem.entity.Order;
import pl.com.possystem.payload.dto.OrderDTO;

import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderDTO toDTO(Order order){
        return OrderDTO.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .branchId(order.getBranch().getId())
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .branch(BranchMapper.toDTO(order.getBranch()))
                .cashier(UserMapper.toDTO(order.getCashier()))
                .customer(order.getCustomer())
                .paymentType(order.getPaymentType())
                .items(order.getItems().stream().map(OrderItemMapper::toDTO).collect(Collectors.toList()))
                .build();
    }
}
