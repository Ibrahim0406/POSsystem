package pl.com.possystem.service.impl;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.com.possystem.entity.Branch;
import pl.com.possystem.entity.Inventory;
import pl.com.possystem.entity.Product;
import pl.com.possystem.mapper.InventoryMapper;
import pl.com.possystem.payload.dto.InventoryDTO;
import pl.com.possystem.repository.BranchRepository;
import pl.com.possystem.repository.InventoryRepository;
import pl.com.possystem.repository.ProductRepository;
import pl.com.possystem.service.InventoryService;
import pl.com.possystem.service.TenantAccess;
import pl.com.possystem.service.UserService;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final BranchRepository branchRepository;

    private final InventoryRepository inventoryRepository;

    private final ProductRepository productRepository;
    private final UserService userService;

    @Override
    public InventoryDTO createInventory(InventoryDTO inventoryDTO) throws Exception {
        Branch branch = branchRepository.findById(inventoryDTO.getBranchId()).orElseThrow(() -> new Exception("Branch not found"));

        Product product = productRepository.findById(inventoryDTO.getProductId()).orElseThrow(() -> new Exception("Product not found"));
        TenantAccess.requireBranchAccess(userService.getCurrentUser(), branch);
        if (product.getStore() == null || branch.getStore() == null
                || !product.getStore().getId().equals(branch.getStore().getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Product does not belong to this branch's store");
        }

        Inventory inventory = InventoryMapper.toEntity(inventoryDTO, branch, product);

        Inventory savedInventory = inventoryRepository.save(inventory);

        return InventoryMapper.toDTO(savedInventory);
    }

    @Override
    public InventoryDTO updateInventory(Long id, InventoryDTO inventoryDTO) throws Exception {

        Inventory inventory = inventoryRepository.findById(id).orElseThrow(() -> new Exception("Inventory not found"));
        TenantAccess.requireBranchAccess(userService.getCurrentUser(), inventory.getBranch());

        inventory.setQuantity(inventoryDTO.getQuantity());

        Inventory updatedInventory = inventoryRepository.save(inventory);

        return InventoryMapper.toDTO(updatedInventory);
    }

    @Override
    public void deleteInventory(Long id) throws Exception {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(() -> new Exception("Inventory not found"));
        TenantAccess.requireBranchAccess(userService.getCurrentUser(), inventory.getBranch());
        inventoryRepository.delete(inventory);
    }

    @Override
    public InventoryDTO getInventoryById(Long id) throws Exception {

        Inventory inventory = inventoryRepository.findById(id).orElseThrow(() -> new Exception("Inventory not found"));
        TenantAccess.requireBranchAccess(userService.getCurrentUser(), inventory.getBranch());
        return InventoryMapper.toDTO(inventory);
    }

    @Override
    public InventoryDTO getInventoryByProductIdAndBranchId(Long productId, Long branchId) throws Exception {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new Exception("Branch not found"));
        TenantAccess.requireBranchAccess(userService.getCurrentUser(), branch);
        Inventory inventory = inventoryRepository.findByProductIdAndBranchId(productId, branchId);
        if (inventory == null) {
            throw new Exception("Inventory not found");
        }
        return InventoryMapper.toDTO(inventory);
    }

    @Override
    public List<InventoryDTO> getAllInventoriesByBranchId(Long branchId) throws Exception {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Branch not found"));
        TenantAccess.requireBranchAccess(userService.getCurrentUser(), branch);
        List<Inventory> inventories = inventoryRepository.findByBranchId(branchId);
        return inventories.stream().map(InventoryMapper::toDTO).collect(Collectors.toList());
    }
}
