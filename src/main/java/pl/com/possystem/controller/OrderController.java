package pl.com.possystem.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.*;
import pl.com.possystem.domain.OrderStatus;
import pl.com.possystem.domain.PaymentType;
import pl.com.possystem.payload.dto.OrderDTO;
import pl.com.possystem.service.OrderService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    public final OrderService orderService;
    private final PathMatcher pathMatcher;

    @PostMapping("/create")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) throws Exception {
        OrderDTO createdOrder = orderService.createOrder(orderDTO);
        return ResponseEntity.ok(createdOrder);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) throws Exception {
        OrderDTO orderDTO = orderService.getOrderById(id);
        return ResponseEntity.ok(orderDTO);
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByBranch(@PathVariable Long branchId,
                                                      @RequestParam(required = false) Long customerId,
                                                      @RequestParam(required = false) Long cashierId,
                                                      @RequestParam(required = false) PaymentType paymentType,
                                                      @RequestParam(required = false) OrderStatus status
                                                      ) throws Exception {
        List<OrderDTO> orders = orderService.getOrderByBranch(branchId, customerId, cashierId, paymentType, status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/cashier/{id}")
    public ResponseEntity<List<OrderDTO>> getOrdersByCashier(@PathVariable Long id) throws Exception {
        List<OrderDTO> orders = orderService.getOrderByCashier(id);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/today/branch/{id}")
    public ResponseEntity<List<OrderDTO>> getTodayOrdersByBranch(@PathVariable Long id) throws Exception {
        List<OrderDTO> orders = orderService.getTodayOrdersByBranch(id);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<List<OrderDTO>> getOrdersByCustomerId(@PathVariable Long id) throws Exception {
        List<OrderDTO> orders = orderService.getOrdersByCustomerId(id);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/recent/{branchId}")
    public ResponseEntity<List<OrderDTO>> getTop5RecentOrdersByBranchId(@PathVariable Long branchId) throws Exception {
        List<OrderDTO> orders = orderService.getTop5RecentOrdersByBranchId(branchId);
        return ResponseEntity.ok(orders);
    }


}
