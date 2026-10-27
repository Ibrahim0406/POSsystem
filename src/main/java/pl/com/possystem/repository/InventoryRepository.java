package pl.com.possystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.com.possystem.entity.Inventory;

import java.util.List;


public interface InventoryRepository extends JpaRepository<Inventory, Long>
{
    Inventory findByProductIdAndBranchId(Long productId, Long branchId);
    List<Inventory> findByBranchId(Long branchId);
}
