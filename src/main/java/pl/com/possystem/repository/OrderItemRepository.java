package pl.com.possystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.possystem.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
