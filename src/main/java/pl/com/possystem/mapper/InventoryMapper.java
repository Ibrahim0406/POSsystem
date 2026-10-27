package pl.com.possystem.mapper;


import pl.com.possystem.entity.Branch;
import pl.com.possystem.entity.Inventory;
import pl.com.possystem.entity.Product;
import pl.com.possystem.payload.dto.InventoryDTO;

public class InventoryMapper {

    public static InventoryDTO toDTO(Inventory inventory) {
        return InventoryDTO.builder()
                .id(inventory.getId())
                .branch(BranchMapper.toDTO(inventory.getBranch()))
                .branchId(inventory.getBranch().getId())
                .product(ProductMapper.toDTO(inventory.getProduct()))
                .productId(inventory.getProduct().getId())
                .quantity(inventory.getQuantity())
                .lastUpdated(inventory.getLastUpdated())
                .build();
    }

    public static Inventory toEntity(InventoryDTO inventoryDTO, Branch branch, Product product) {
        return Inventory.builder()
                .branch(branch)
                .product(product)
                .quantity(inventoryDTO.getQuantity())
                .build();
    }

}
