package pl.com.possystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.possystem.entity.Refund;
import pl.com.possystem.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByCashierIdAndCreatedAtBetween(Long cashierId, LocalDateTime startDate, LocalDateTime endDate);

    List<Refund> findByCashierId(Long id);

    List<Refund> findByShiftReportId(Long id);

    List<Refund> findByBranchId(Long id);

}
