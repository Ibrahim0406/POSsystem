package pl.com.possystem.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.com.possystem.entity.ShiftReport;
import pl.com.possystem.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import pl.com.possystem.domain.UserRole;

public interface ShiftReportRepository extends JpaRepository<ShiftReport, Long> {

    List<ShiftReport> findByCashierId(Long id);

    List<ShiftReport> findByBranchId(Long id);

    List<ShiftReport> findByBranchStoreId(Long storeId);

    Optional<ShiftReport> findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(User cashier);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT report FROM ShiftReport report
            WHERE report.cashier = :cashier AND report.shiftEnd IS NULL
            ORDER BY report.shiftStart DESC
            """)
    List<ShiftReport> findActiveByCashierForUpdate(
            @Param("cashier") User cashier,
            Pageable pageable);

    Optional<ShiftReport> findByCashierAndShiftStartBetween(User cashier, LocalDateTime startOfDay, LocalDateTime endOfDay);


    @Query("SELECT sr FROM ShiftReport sr " +
            "WHERE sr.branch.id = :branchId " +
            "AND sr.cashier.role = :cashierRole " +
            "AND sr.shiftEnd IS NULL " +
            "ORDER BY sr.shiftStart DESC")
    List<ShiftReport> findActiveCashierShiftsByBranch(
            @Param("branchId") Long branchId,
            @Param("cashierRole") UserRole cashierRole);
    
}
