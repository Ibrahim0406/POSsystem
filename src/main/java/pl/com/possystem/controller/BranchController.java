package pl.com.possystem.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.possystem.payload.dto.BranchDTO;
import pl.com.possystem.payload.response.ApiResponse;
import pl.com.possystem.service.BranchService;
import pl.com.possystem.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;

    private final UserService userService;


    @PostMapping("/create")
    public ResponseEntity<BranchDTO> createBranch(@RequestBody BranchDTO branchDTO) throws Exception {
        BranchDTO createdBranch = branchService.createBranch(branchDTO);
        return ResponseEntity.ok(createdBranch);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchDTO> getBranchById(@PathVariable Long id) throws Exception {
        BranchDTO getBranchById = branchService.getBranchById(id);
        return ResponseEntity.ok(getBranchById);
    }


    @GetMapping("/store/{storeId}")
    public ResponseEntity <List<BranchDTO>> getBranchByStoreId(@PathVariable Long storeId) throws Exception {
        List<BranchDTO> getBranchById = branchService.getAllBranchesByStoreId(storeId);
        return ResponseEntity.ok(getBranchById);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<BranchDTO> updateBranch(@PathVariable Long id, @RequestBody BranchDTO branchDTO) throws Exception {
        BranchDTO updatedBranch = branchService.updateBranch(id, branchDTO);
        return ResponseEntity.ok(updatedBranch);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteBranch(@PathVariable Long id) throws Exception {
        branchService.deleteBranch(id);
        ApiResponse response = new ApiResponse();
        response.setMessage("Branch deleted successfully");
        return ResponseEntity.ok(response);
    }

}
