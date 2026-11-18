package pl.com.possystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.com.possystem.entity.Customer;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByStoreId(Long storeId);

    @Query("SELECT c FROM Customer c WHERE c.store.id = :storeId AND "
            + "(LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Customer> searchByStoreIdAndKeyword(
            @Param("storeId") Long storeId,
            @Param("keyword") String keyword);
}
