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

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final BranchRepository branchRepository;

    private final InventoryRepository inventoryRepository;

    private final ProductRepository productRepository;

    @Override
    public InventoryDTO createInventory(InventoryDTO inventoryDTO) throws Exception {
        Branch branch = branchRepository.findById(inventoryDTO.getBranchId()).orElseThrow(() -> new Exception("Branch not found"));

        Product product = productRepository.findById(inventoryDTO.getProductId()).orElseThrow(() -> new Exception("Product not found"));

        Inventory inventory = InventoryMapper.toEntity(inventoryDTO, branch, product);

        Inventory savedInventory = inventoryRepository.save(inventory);

        return InventoryMapper.toDTO(savedInventory);
    }

    @Override
    public InventoryDTO updateInventory(Long id, InventoryDTO inventoryDTO) throws Exception {

        Inventory inventory = inventoryRepository.findById(id).orElseThrow(() -> new Exception("Inventory not found"));

        inventory.setQuantity(inventoryDTO.getQuantity());

        Inventory updatedInventory = inventoryRepository.save(inventory);

        return InventoryMapper.toDTO(updatedInventory);
    }

    @Override
    public void deleteInventory(Long id) throws Exception {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(() -> new Exception("Inventory not found"));
        inventoryRepository.delete(inventory);
    }

    @Override
    public InventoryDTO getInventoryById(Long id) throws Exception {

        Inventory inventory = inventoryRepository.findById(id).orElseThrow(() -> new Exception("Inventory not found"));
        return InventoryMapper.toDTO(inventory);
    }

    @Override
    public InventoryDTO getInventoryByProductIdAndBranchId(Long productId, Long branchId) throws Exception {
        Inventory inventory = inventoryRepository.findByProductIdAndBranchId(productId, branchId);
        if (inventory == null) {
            throw new Exception("Inventory not found");
        }
        return InventoryMapper.toDTO(inventory);
    }

    @Override
    public List<InventoryDTO> getAllInventoriesByBranchId(Long branchId){
        List<Inventory> inventories = inventoryRepository.findByBranchId(branchId);
        return inventories.stream().map(InventoryMapper::toDTO).collect(Collectors.toList());
    }
}
