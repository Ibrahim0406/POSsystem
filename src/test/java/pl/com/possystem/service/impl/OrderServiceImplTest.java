package pl.com.possystem.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.com.possystem.entity.*;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.payload.dto.OrderDTO;
import pl.com.possystem.payload.dto.OrderItemDTO;
import pl.com.possystem.repository.InventoryRepository;
import pl.com.possystem.repository.OrderRepository;
import pl.com.possystem.repository.ProductRepository;
import pl.com.possystem.repository.BranchRepository;
import pl.com.possystem.repository.CustomerRepository;
import pl.com.possystem.repository.UserRepository;
import pl.com.possystem.service.UserService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    private OrderRepository orderRepository;
    private UserService userService;
    private ProductRepository productRepository;
    private InventoryRepository inventoryRepository;
    private OrderServiceImpl orderService;
    private User cashier;
    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() throws Exception {
        orderRepository = mock(OrderRepository.class);
        userService = mock(UserService.class);
        productRepository = mock(ProductRepository.class);
        inventoryRepository = mock(InventoryRepository.class);
        BranchRepository branchRepository = mock(BranchRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        orderService = new OrderServiceImpl(
                orderRepository, userService, productRepository, inventoryRepository,
                branchRepository, customerRepository, userRepository
        );

        Store store = new Store();
        store.setId(10L);

        Branch branch = new Branch();
        branch.setId(20L);
        branch.setStore(store);

        cashier = new User();
        cashier.setId(30L);
        cashier.setBranch(branch);
        cashier.setRole(UserRole.ROLE_BRANCH_CASHIER);

        Category category = new Category();
        category.setName("Food");

        product = new Product();
        product.setId(40L);
        product.setStore(store);
        product.setCategory(category);
        product.setSellingPrice(new BigDecimal("0.10"));

        inventory = new Inventory();
        inventory.setBranch(branch);
        inventory.setProduct(product);
        inventory.setQuantity(5);

        when(userService.getCurrentUser()).thenReturn(cashier);
        when(productRepository.findById(40L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdAndBranchIdForUpdate(40L, 20L))
                .thenReturn(Optional.of(inventory));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(50L);
            return order;
        });
    }

    @Test
    void createsOrderWithExactMoneyAndDecrementsAggregatedQuantity() throws Exception {
        OrderDTO request = OrderDTO.builder()
                .items(List.of(item(1), item(2)))
                .build();

        OrderDTO result = orderService.createOrder(request);

        assertEquals(new BigDecimal("0.30"), result.getTotalAmount());
        assertEquals(2, inventory.getQuantity());
        assertEquals(new BigDecimal("0.10"), result.getItems().get(0).getPrice());
        assertEquals(new BigDecimal("0.20"), result.getItems().get(1).getPrice());
        verify(inventoryRepository, times(1)).findByProductIdAndBranchIdForUpdate(40L, 20L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void rejectsNonPositiveQuantityBeforeLoadingProduct() {
        OrderDTO request = OrderDTO.builder().items(List.of(item(0))).build();

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request));

        verifyNoInteractions(productRepository, inventoryRepository, orderRepository);
    }

    @Test
    void rejectsProductFromAnotherStore() {
        Store otherStore = new Store();
        otherStore.setId(11L);
        product.setStore(otherStore);

        OrderDTO request = OrderDTO.builder().items(List.of(item(1))).build();

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request));

        verifyNoInteractions(inventoryRepository, orderRepository);
    }

    @Test
    void rejectsOrderWhenBranchHasInsufficientInventory() {
        inventory.setQuantity(2);
        OrderDTO request = OrderDTO.builder().items(List.of(item(1), item(2))).build();

        assertThrows(IllegalStateException.class, () -> orderService.createOrder(request));

        assertEquals(2, inventory.getQuantity());
        verifyNoInteractions(orderRepository);
    }

    private OrderItemDTO item(int quantity) {
        return OrderItemDTO.builder().productId(40L).quantity(quantity).build();
    }
}
