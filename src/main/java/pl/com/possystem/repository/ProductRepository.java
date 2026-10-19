package pl.com.possystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.com.possystem.entity.Product;

import java.util.Optional;

@Repository
public interface ProductRepository  extends JpaRepository<Product, Long> {

    Optional<Product> findByBarcode(String barcode);

}
