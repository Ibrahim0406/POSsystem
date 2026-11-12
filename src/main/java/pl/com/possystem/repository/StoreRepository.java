package pl.com.possystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.possystem.entity.Store;
import pl.com.possystem.domain.StoreStatus;
import java.util.List;


public interface StoreRepository extends JpaRepository<Store, Long> {

    Store findByStoreAdminId(Long adminId);

    List<Store> findByBrandContainingIgnoreCase(String brand);

    long countByStatus(StoreStatus status);

}
