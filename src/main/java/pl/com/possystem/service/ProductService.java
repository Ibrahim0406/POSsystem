package pl.com.possystem.service;

import pl.com.possystem.payload.dto.ProductDTO;
import pl.com.possystem.entity.User;

import java.util.List;

public interface ProductService {

    ProductDTO createProduct(ProductDTO productDTO, User user) throws Exception;

    ProductDTO updateProduct(Long id, ProductDTO productDTO, User user) throws Exception;

    void deleteProduct(Long id, User user) throws Exception;

    List<ProductDTO> getAllProductsByStoreId(Long storeId);

    List<ProductDTO> searchByKeyword(Long storeId, String keyword) throws Exception;




}
