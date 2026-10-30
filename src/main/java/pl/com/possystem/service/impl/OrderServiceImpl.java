package pl.com.possystem.service.impl;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.com.possystem.domain.OrderStatus;
import pl.com.possystem.domain.PaymentType;
import pl.com.possystem.entity.*;
import pl.com.possystem.mapper.OrderMapper;
import pl.com.possystem.payload.dto.OrderDTO;
import pl.com.possystem.repository.OrderItemRepository;
import pl.com.possystem.repository.OrderRepository;
import pl.com.possystem.repository.ProductRepository;
import pl.com.possystem.service.OrderService;
import pl.com.possystem.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;


    @Override
    public OrderDTO createOrder(OrderDTO orderDTO) throws Exception {

        User cashier = userService.getCurrentUser();

        Branch branch = cashier.getBranch();

        if (branch == null) {
            throw new Exception("Cashier does not belong to any branch.");
        }

        Order order = Order.builder()
                .branch(branch)
                .cashier(cashier)
                .customer(orderDTO.getCustomer())
                .paymentType(orderDTO.getPaymentType())
                .build();


        List<OrderItem> orderItems = orderDTO.getItems().stream().map(
                itemDTO -> {
                    Product product = productRepository.findById(itemDTO.getProductId()).orElseThrow(() -> new EntityNotFoundException("Product not found"));

                    return OrderItem.builder()
                            .product(product)
                            .quantity(itemDTO.getQuantity())
                            .price(product.getSellingPrice() * itemDTO.getQuantity())
                            .order(order)
                            .build();


                }
        ).toList();
        double total = orderItems.stream().mapToDouble(OrderItem::getPrice).sum();
        order.setTotalAmount(total);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        return OrderMapper.toDTO(savedOrder);
    }

    @Override
    public OrderDTO getOrderById(Long id) throws Exception {
        return orderRepository.findById(id).map(OrderMapper::toDTO).orElseThrow( () -> new Exception("Order not found"));
    }

    @Override
    public List<OrderDTO> getOrderByBranch(Long branchId, Long customerId, Long cashierId, PaymentType paymentType, OrderStatus status) throws Exception {

        return orderRepository.findByBranchId(branchId)
                .stream()
                .filter(order -> customerId == null || (order.getCustomer()!= null && order.getCustomer().getId().equals(customerId)))
                .filter(order -> cashierId == null || order.getCashier().getId().equals(cashierId) && order.getCustomer().getId().equals(cashierId))
                .filter(order -> paymentType == null || order.getPaymentType().equals(paymentType))

                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrderByCashier(Long cashierId) throws Exception {
        return orderRepository.findByCashierId(cashierId)
                .stream()
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteOrder(Long orderId) throws Exception {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new Exception("Order not found"));

        orderRepository.delete(order);
    }

    @Override
    public List<OrderDTO> getTodayOrdersByBranch(Long branchId) throws Exception {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        return orderRepository.findByBranchIdAndCreatedAtBetween(branchId, start, end).stream()
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByCustomerId(Long customerId) throws Exception {

        return orderRepository.findByCustomerId(customerId).stream()
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getTop5RecentOrdersByBranchId(Long branchId) throws Exception {
        return orderRepository.findTop5ByBranchIdOrderByCreatedAtDesc(branchId).stream()
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

}
