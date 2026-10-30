package pl.com.possystem.payload.dto;

import lombok.Builder;
import lombok.Data;
import pl.com.possystem.domain.PaymentType;
import pl.com.possystem.entity.Customer;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderDTO {

    private Long id;

    private Double totalAmount;

    private LocalDateTime createdAt;

    private Long branchId;

    private Long customerId;

    private BranchDTO branch;

    private UserDTO cashier;

    private Customer customer;

    private PaymentType paymentType;

    private List<OrderItemDTO> items;
}
