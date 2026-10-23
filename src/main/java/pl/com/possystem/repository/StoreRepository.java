package pl.com.possystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.possystem.entity.Store;


public interface StoreRepository extends JpaRepository<Store, Long> {

    Store findByStoreAdminId(Long adminId);


}
