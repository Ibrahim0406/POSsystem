package pl.com.possystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.com.possystem.entity.Branch;

import java.util.List;


public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findByStoreId(Long storeId);

}
