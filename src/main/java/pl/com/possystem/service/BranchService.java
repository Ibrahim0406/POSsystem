package pl.com.possystem.service;

import pl.com.possystem.entity.Branch;
import pl.com.possystem.entity.User;
import pl.com.possystem.payload.dto.BranchDTO;

import java.util.List;

public interface BranchService {

    BranchDTO createBranch(BranchDTO branchDTO) throws Exception;

    BranchDTO updateBranch(Long id, BranchDTO branchDTO) throws Exception;

    void deleteBranch(Long id) throws Exception;

    List<BranchDTO> getAllBranchesByStoreId(Long storeId);

    BranchDTO getBranchById(Long id) throws Exception;
}
