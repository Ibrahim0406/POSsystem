package pl.com.possystem.mapper;

import pl.com.possystem.entity.Category;
import pl.com.possystem.entity.Product;
import pl.com.possystem.entity.Store;
import pl.com.possystem.payload.dto.ProductDTO;

public class ProductMapper {

    public static ProductDTO toDTO(Product product) {


        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .mrp(product.getMrp())
                .sellingPrice(product.getSellingPrice())
                .description(product.getDescription())
                .brand(product.getBrand())
                .imageUrl(product.getImageUrl())
                .storeId(product.getStore() != null ? product.getStore().getId() : null)
                .category(CategoryMapper.toDTO(product.getCategory()))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public static Product toEntity(ProductDTO productDTO, Store store, Category category) {
        return Product.builder()
                .name(productDTO.getName())
                .sku(productDTO.getSku())
                .mrp(productDTO.getMrp())
                .sellingPrice(productDTO.getSellingPrice())
                .description(productDTO.getDescription())
                .brand(productDTO.getBrand())
                .imageUrl(productDTO.getImageUrl())
                .store(store)
                .category(category) // Assuming you have a method to fetch the category entity
                .createdAt(productDTO.getCreatedAt())
                .updatedAt(productDTO.getUpdatedAt())
                .build();
    }
}
