package pl.com.possystem.service;

import pl.com.possystem.domain.OrderStatus;
import pl.com.possystem.domain.PaymentType;
import pl.com.possystem.payload.dto.OrderDTO;

import java.util.List;

public interface OrderService {

    OrderDTO createOrder(OrderDTO orderDTO) throws Exception;

    OrderDTO getOrderById(Long id) throws Exception;

    List<OrderDTO> getOrderByBranch(Long branchId, Long customerId, Long cashierId, PaymentType paymentType, OrderStatus status) throws Exception;

    List<OrderDTO> getOrderByCashier(Long cashierId) throws Exception;

    void deleteOrder(Long orderId) throws Exception;

    List<OrderDTO> getTodayOrdersByBranch(Long branchId) throws Exception;

    List<OrderDTO> getOrdersByCustomerId(Long customerId) throws Exception;

    List<OrderDTO> getTop5RecentOrdersByBranchId(Long branchId) throws Exception;

}
