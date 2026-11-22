package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.com.possystem.entity.Branch;
import pl.com.possystem.entity.Store;
import pl.com.possystem.entity.User;
import pl.com.possystem.mapper.BranchMapper;
import pl.com.possystem.payload.dto.BranchDTO;
import pl.com.possystem.repository.BranchRepository;
import pl.com.possystem.repository.StoreRepository;
import pl.com.possystem.repository.UserRepository;
import pl.com.possystem.service.BranchService;
import pl.com.possystem.service.TenantAccess;
import pl.com.possystem.service.UserService;
import pl.com.possystem.domain.UserRole;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final StoreRepository storeRepository;

    private final UserRepository userRepository;
    private final UserService userService;
    private final TenantAccess tenantAccess;

    @Override
    public BranchDTO createBranch(BranchDTO branchDTO) throws Exception {

        User currentUser = userService.getCurrentUser();
        Store store = tenantAccess.requireActorStore(currentUser);
        tenantAccess.requireStoreManagement(currentUser, store);
        Branch branch = BranchMapper.toEntity(branchDTO, store);
        Branch savedBranch = branchRepository.save(branch);

        return BranchMapper.toDTO(savedBranch);
    }

    @Override
    public BranchDTO updateBranch(Long id, BranchDTO branchDTO) throws Exception {
        User currentUser = userService.getCurrentUser();
        Branch branch = branchRepository.findById(id).orElseThrow(() -> new Exception("Branch not found"));
        tenantAccess.requireBranchManagement(currentUser, branch);

        branch.setName(branchDTO.getName());
        branch.setAddress(branchDTO.getAddress());
        branch.setPhoneNumber(branchDTO.getPhoneNumber());
        branch.setEmail(branchDTO.getEmail());
        branch.setWorkingDays(branchDTO.getWorkingDays());
        branch.setOpenTime(branchDTO.getOpenTime());
        branch.setCloseTime(branchDTO.getCloseTime());
        branch.setUpdatedAt(LocalDateTime.now());

        Branch savedBranch = branchRepository.save(branch);

        return BranchMapper.toDTO(savedBranch);
    }

    @Override
    public void deleteBranch(Long id) throws Exception {
        Branch branch = branchRepository.findById(id).orElseThrow(() -> new Exception("Branch not found"));
        tenantAccess.requireBranchManagement(userService.getCurrentUser(), branch);
        branchRepository.delete(branch);
    }

    @Override
    public List<BranchDTO> getAllBranchesByStoreId(Long storeId) throws Exception {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Store not found"));
        User actor = userService.getCurrentUser();
        tenantAccess.requireStoreAccess(actor, store);
        if (actor.getRole() == UserRole.ROLE_BRANCH_MANAGER
                || actor.getRole() == UserRole.ROLE_BRANCH_CASHIER) {
            Branch assignedBranch = actor.getBranch();
            if (assignedBranch == null || assignedBranch.getStore() == null
                    || !java.util.Objects.equals(assignedBranch.getStore().getId(), store.getId())) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "The current user is not assigned to this store");
            }
            return List.of(BranchMapper.toDTO(assignedBranch));
        }
        List<Branch> branches = branchRepository.findByStoreId(storeId);
        return branches.stream().map(BranchMapper::toDTO).toList();
    }

    @Override
    public BranchDTO getBranchById(Long id) throws Exception {

        Branch branch =  branchRepository.findById(id).orElseThrow(() -> new Exception("Branch not found"));
        tenantAccess.requireBranchAccess(userService.getCurrentUser(), branch);
        return BranchMapper.toDTO(branch);
    }
}
