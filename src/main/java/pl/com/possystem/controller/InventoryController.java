package pl.com.possystem.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.possystem.payload.dto.InventoryDTO;
import pl.com.possystem.payload.response.ApiResponse;
import pl.com.possystem.service.InventoryService;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/create")
    public ResponseEntity<InventoryDTO> createInventory(@RequestBody InventoryDTO inventoryDTO) throws Exception {
        InventoryDTO createdInventory = inventoryService.createInventory(inventoryDTO);
        return ResponseEntity.ok(createdInventory);
    }

    @PutMapping("/update")
    public ResponseEntity<InventoryDTO> updateInventory(@RequestBody InventoryDTO inventoryDTO, @RequestParam Long id) throws Exception {
        InventoryDTO updatedInventory = inventoryService.updateInventory(id, inventoryDTO);
        return ResponseEntity.ok(updatedInventory);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse> deleteInventory(@RequestParam Long id) throws Exception {
        inventoryService.deleteInventory(id);
        ApiResponse response = new ApiResponse();
        response.setMessage("Inventory deleted successfully");
        return ResponseEntity.ok(response);

    }


    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<InventoryDTO>> getInventoryByBranchId(@PathVariable Long branchId) throws Exception {
        List<InventoryDTO> getInventory = inventoryService.getAllInventoriesByBranchId(branchId);
        return ResponseEntity.ok(getInventory);
    }


    @GetMapping("/branch/{branchId}/product/{productId}")
    public ResponseEntity<List<InventoryDTO>> getInventoryByProductIdAndBranchId(@PathVariable Long productId, @PathVariable Long branchId) throws Exception {
      InventoryDTO inventoryDTO = inventoryService.getInventoryByProductIdAndBranchId(productId, branchId);
        return ResponseEntity.ok(List.of(inventoryDTO));
    }


}
