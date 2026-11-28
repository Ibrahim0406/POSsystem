package pl.com.possystem.service.impl;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.com.possystem.domain.OrderStatus;
import pl.com.possystem.domain.PaymentType;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.entity.*;
import pl.com.possystem.mapper.OrderMapper;
import pl.com.possystem.payload.dto.OrderDTO;
import pl.com.possystem.repository.OrderRepository;
import pl.com.possystem.repository.InventoryRepository;
import pl.com.possystem.repository.ProductRepository;
import pl.com.possystem.repository.BranchRepository;
import pl.com.possystem.repository.CustomerRepository;
import pl.com.possystem.repository.ShiftReportRepository;
import pl.com.possystem.repository.UserRepository;
import pl.com.possystem.service.OrderService;
import pl.com.possystem.service.TenantAccess;
import pl.com.possystem.service.UserService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final BranchRepository branchRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final TenantAccess tenantAccess;
    private final ShiftReportRepository shiftReportRepository;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO createOrder(OrderDTO orderDTO) throws Exception {

        if (orderDTO == null || orderDTO.getItems() == null || orderDTO.getItems().isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one item.");
        }

        User cashier = userService.getCurrentUser();

        Branch branch = cashier.getBranch();

        if (branch == null || branch.getStore() == null) {
            throw new Exception("Cashier does not belong to any branch.");
        }
        tenantAccess.requireBranchAccess(cashier, branch);
        if (cashier.getRole() == UserRole.ROLE_BRANCH_CASHIER) {
            ShiftReport activeShift = shiftReportRepository
                    .findActiveByCashierForUpdate(cashier, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.CONFLICT, "An active shift is required to create an order"));
            if (activeShift.getBranch() == null
                    || !branch.getId().equals(activeShift.getBranch().getId())) {
                throw new IllegalStateException("The active shift does not belong to the cashier's branch");
            }
        }

        Map<Long, Integer> requestedQuantities = new TreeMap<>();
        for (var itemDTO : orderDTO.getItems()) {
            if (itemDTO == null || itemDTO.getProductId() == null) {
                throw new IllegalArgumentException("Each order item must specify a product.");
            }
            if (itemDTO.getQuantity() == null || itemDTO.getQuantity() <= 0) {
                throw new IllegalArgumentException("Order item quantities must be positive.");
            }
            try {
                requestedQuantities.merge(itemDTO.getProductId(), itemDTO.getQuantity(), Math::addExact);
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException("The requested quantity is too large.", exception);
            }
        }

        Map<Long, Product> products = new TreeMap<>();
        Map<Long, Inventory> inventories = new TreeMap<>();
        for (Map.Entry<Long, Integer> requested : requestedQuantities.entrySet()) {
            Long productId = requested.getKey();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

            if (product.getStore() == null
                    || !product.getStore().getId().equals(branch.getStore().getId())) {
                throw new IllegalArgumentException("Product does not belong to the cashier's store: " + productId);
            }
            if (product.getSellingPrice() == null || product.getSellingPrice().signum() < 0) {
                throw new IllegalStateException("Product has an invalid selling price: " + productId);
            }

            Inventory inventory = inventoryRepository.findByProductIdAndBranchIdForUpdate(productId, branch.getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "No inventory is configured for product " + productId + " at this branch."
                    ));
            if (inventory.getQuantity() == null || inventory.getQuantity() < requested.getValue()) {
                throw new IllegalStateException("Insufficient inventory for product: " + productId);
            }

            products.put(productId, product);
            inventories.put(productId, inventory);
        }

        Order order = Order.builder()
                .branch(branch)
                .cashier(cashier)
                .paymentType(orderDTO.getPaymentType())
                .build();
        Long customerId = orderDTO.getCustomerId() != null
                ? orderDTO.getCustomerId()
                : orderDTO.getCustomer() == null ? null : orderDTO.getCustomer().getId();
        if (customerId != null) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
            tenantAccess.requireCustomerAccess(cashier, customer);
            order.setCustomer(customer);
        }


        List<OrderItem> orderItems = orderDTO.getItems().stream().map(
                itemDTO -> {
                    Product product = products.get(itemDTO.getProductId());
                    BigDecimal price = product.getSellingPrice()
                            .setScale(2, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

                    return OrderItem.builder()
                            .product(product)
                            .quantity(itemDTO.getQuantity())
                            .price(price)
                            .order(order)
                            .build();


                }
        ).toList();
        BigDecimal total = orderItems.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
        order.setItems(orderItems);

        for (Map.Entry<Long, Inventory> inventoryEntry : inventories.entrySet()) {
            int quantity = requestedQuantities.get(inventoryEntry.getKey());
            inventoryEntry.getValue().setQuantity(inventoryEntry.getValue().getQuantity() - quantity);
        }

        Order savedOrder = orderRepository.save(order);

        return OrderMapper.toDTO(savedOrder);
    }

    @Override
    public OrderDTO getOrderById(Long id) throws Exception {
        Order order = orderRepository.findById(id).orElseThrow(() -> new Exception("Order not found"));
        User actor = userService.getCurrentUser();
        tenantAccess.requireBranchAccess(actor, order.getBranch());
        if (actor.getRole() == UserRole.ROLE_BRANCH_CASHIER
                && (order.getCashier() == null
                || !java.util.Objects.equals(actor.getId(), order.getCashier().getId()))) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Cashiers can only view their own orders");
        }
        return OrderMapper.toDTO(order);
    }

    @Override
    public List<OrderDTO> getOrderByBranch(Long branchId, Long customerId, Long cashierId, PaymentType paymentType, OrderStatus status) throws Exception {
        User actor = userService.getCurrentUser();
        tenantAccess.requireBranchAccess(actor,
                branchRepository.findById(branchId).orElseThrow(() -> new Exception("Branch not found")));

        return orderRepository.findByBranchId(branchId)
                .stream()
                .filter(order -> actor.getRole() != UserRole.ROLE_BRANCH_CASHIER
                        || (order.getCashier() != null
                        && java.util.Objects.equals(actor.getId(), order.getCashier().getId())))
                .filter(order -> customerId == null || (order.getCustomer()!= null && order.getCustomer().getId().equals(customerId)))
                .filter(order -> cashierId == null || order.getCashier() != null
                        && order.getCashier().getId().equals(cashierId))
                .filter(order -> paymentType == null || paymentType.equals(order.getPaymentType()))

                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrderByCashier(Long cashierId) throws Exception {
        User cashier = userRepository.findById(cashierId).orElseThrow(() -> new Exception("Cashier not found"));
        tenantAccess.requireUserAccess(userService.getCurrentUser(), cashier);
        return orderRepository.findByCashierId(cashierId)
                .stream()
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteOrder(Long orderId) throws Exception {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new Exception("Order not found"));
        tenantAccess.requireBranchManagement(userService.getCurrentUser(), order.getBranch());

        orderRepository.delete(order);
    }

    @Override
    public List<OrderDTO> getTodayOrdersByBranch(Long branchId) throws Exception {
        User actor = userService.getCurrentUser();
        tenantAccess.requireBranchAccess(actor,
                branchRepository.findById(branchId).orElseThrow(() -> new Exception("Branch not found")));
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        return orderRepository.findByBranchIdAndCreatedAtBetween(branchId, start, end).stream()
                .filter(order -> actor.getRole() != UserRole.ROLE_BRANCH_CASHIER
                        || (order.getCashier() != null
                        && java.util.Objects.equals(actor.getId(), order.getCashier().getId())))
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByCustomerId(Long customerId) throws Exception {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new Exception("Customer not found"));
        tenantAccess.requireCustomerAccess(userService.getCurrentUser(), customer);

        return orderRepository.findByCustomerId(customerId).stream()
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getTop5RecentOrdersByBranchId(Long branchId) throws Exception {
        User actor = userService.getCurrentUser();
        tenantAccess.requireBranchAccess(actor,
                branchRepository.findById(branchId).orElseThrow(() -> new Exception("Branch not found")));
        return orderRepository.findTop5ByBranchIdOrderByCreatedAtDesc(branchId).stream()
                .filter(order -> actor.getRole() != UserRole.ROLE_BRANCH_CASHIER
                        || (order.getCashier() != null
                        && java.util.Objects.equals(actor.getId(), order.getCashier().getId())))
                .map(OrderMapper::toDTO)
                .collect(Collectors.toList());
    }

}
