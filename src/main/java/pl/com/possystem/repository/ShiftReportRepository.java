package pl.com.possystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.possystem.entity.ShiftReport;
import pl.com.possystem.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShiftReportRepository extends JpaRepository<ShiftReport, Long> {

    List<ShiftReport> findByCashierId(Long id);

    List<ShiftReport> findByBranchId(Long id);

    Optional<ShiftReport> findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(User cashier);

    Optional<ShiftReport> findByCashierAndShiftStartBetween(User cashier, LocalDateTime startOfDay, LocalDateTime endOfDay);


}
