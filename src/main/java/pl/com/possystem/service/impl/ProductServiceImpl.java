package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import pl.com.possystem.entity.Category;
import pl.com.possystem.entity.Product;
import pl.com.possystem.entity.Store;
import pl.com.possystem.entity.User;
import pl.com.possystem.mapper.ProductMapper;
import pl.com.possystem.payload.dto.ProductDTO;
import pl.com.possystem.repository.CategoryRepository;
import pl.com.possystem.repository.ProductRepository;
import pl.com.possystem.repository.StoreRepository;
import pl.com.possystem.service.ProductService;
import pl.com.possystem.service.TenantAccess;
import pl.com.possystem.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {


    private final ProductRepository productRepository;

    private final StoreRepository storeRepository;

    private final CategoryRepository categoryRepository;

    private final UserService userService;

    @Override
    public ProductDTO createProduct(ProductDTO productDTO, User user) throws Exception {

        Store store = storeRepository.findById(productDTO.getStoreId()).orElseThrow(() -> new Exception("Store not found"));
        TenantAccess.requireStoreAccess(user, store);

        Category category = categoryRepository.findById(productDTO.getCategoryId()).orElseThrow(() -> new Exception("Category not found"));
        if (category.getStore() == null || !category.getStore().getId().equals(store.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Category does not belong to this store");
        }
        Product product = ProductMapper.toEntity(productDTO, store, category);
        Product savedProduct = productRepository.save(product);

        return ProductMapper.toDTO(savedProduct);
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductDTO productDTO, User user) throws Exception {
        Product product = productRepository.findById(id).orElseThrow(() -> new Exception("Product not found"));
        TenantAccess.requireStoreAccess(user, product.getStore());

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setSku(productDTO.getSku());
        product.setImageUrl(productDTO.getImageUrl());
        product.setMrp(productDTO.getMrp());
        product.setSellingPrice(productDTO.getSellingPrice());
        product.setBrand(productDTO.getBrand());
        product.setUpdatedAt(LocalDateTime.now());

        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId()).orElseThrow(() -> new Exception("Category not found"));
            if (category.getStore() == null || product.getStore() == null
                    || !category.getStore().getId().equals(product.getStore().getId())) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Category does not belong to this store");
            }
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);

        return ProductMapper.toDTO(savedProduct);
    }

    @Override
    public void deleteProduct(Long id, User user) throws Exception {
        Product product = productRepository.findById(id).orElseThrow(() -> new Exception("Product not found"));
        TenantAccess.requireStoreAccess(user, product.getStore());
        productRepository.delete(product);
    }

    @Override
    public List<ProductDTO> getAllProductsByStoreId(Long storeId) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("Store not found"));
        TenantAccess.requireStoreAccess(userService.getCurrentUser(), store);
        List<Product> products = productRepository.findByStoreId(storeId);
        return products.stream().map(ProductMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> searchByKeyword(Long storeId, String keyword) throws Exception {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new Exception("Store not found"));
        TenantAccess.requireStoreAccess(userService.getCurrentUser(), store);
        List<Product> products = productRepository.searchByKeyword(storeId, keyword);
        return products.stream().map(ProductMapper::toDTO).collect(Collectors.toList());
    }
}
