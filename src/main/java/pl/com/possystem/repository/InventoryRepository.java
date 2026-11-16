package pl.com.possystem.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.possystem.entity.Inventory;

import java.util.List;
import java.util.Optional;


public interface InventoryRepository extends JpaRepository<Inventory, Long>
{
    Inventory findByProductIdAndBranchId(Long productId, Long branchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.branch.id = :branchId")
    Optional<Inventory> findByProductIdAndBranchIdForUpdate(
            @Param("productId") Long productId,
            @Param("branchId") Long branchId
    );

    List<Inventory> findByBranchId(Long branchId);
}
