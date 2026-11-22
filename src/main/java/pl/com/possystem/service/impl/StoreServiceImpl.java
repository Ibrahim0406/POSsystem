package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.possystem.domain.StoreStatus;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.entity.Store;
import pl.com.possystem.entity.StoreContact;
import pl.com.possystem.entity.User;
import pl.com.possystem.entity.AdminAuditLog;
import pl.com.possystem.exception.UserException;
import pl.com.possystem.mapper.StoreMapper;
import pl.com.possystem.payload.dto.StoreDTO;
import pl.com.possystem.repository.StoreRepository;
import pl.com.possystem.repository.AdminAuditLogRepository;
import pl.com.possystem.repository.UserRepository;
import pl.com.possystem.service.StoreService;
import pl.com.possystem.service.TenantAccess;
import pl.com.possystem.service.UserService;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;

    private final AdminAuditLogRepository auditLogRepository;

    private final UserService userService;
    private final UserRepository userRepository;
    private final TenantAccess tenantAccess;



    @Override
    @Transactional
    public StoreDTO createStore(StoreDTO storeDto, User user) {
        if (user.getRole() != UserRole.ROLE_ADMIN
                && user.getRole() != UserRole.ROLE_STORE_ADMIN) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only system administrators and store administrators can create stores");
        }

        User storeAdmin;
        if (user.getRole() == UserRole.ROLE_ADMIN) {
            if (storeDto.getStoreAdmin() == null || storeDto.getStoreAdmin().getId() == null) {
                throw new IllegalArgumentException(
                        "A store administrator ID is required when a system administrator creates a store");
            }
            storeAdmin = userRepository.findById(storeDto.getStoreAdmin().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Store administrator not found"));
            if (storeAdmin.getRole() != UserRole.ROLE_STORE_ADMIN
                    || storeAdmin.getStore() != null
                    || storeAdmin.getBranch() != null
                    || storeRepository.findByStoreAdminId(storeAdmin.getId()) != null) {
                throw new IllegalArgumentException(
                        "The selected store administrator must be an unassigned store administrator");
            }
        } else {
            if (user.getStore() != null
                    || user.getBranch() != null
                    || storeRepository.findByStoreAdminId(user.getId()) != null) {
                throw new IllegalStateException("The current user is already assigned to a store");
            }
            storeAdmin = user;
        }
        Store store = StoreMapper.toEntity(storeDto, storeAdmin);
        Store savedStore = storeRepository.save(store);
        storeAdmin.setStore(savedStore);
        if (storeAdmin == user) {
            storeAdmin.setRole(UserRole.ROLE_STORE_ADMIN);
        }
        userRepository.save(storeAdmin);
        return StoreMapper.toDTO(savedStore);
    }

    @Override
    public StoreDTO getStoreById(Long id) throws Exception {
        Store store = storeRepository.findById(id).orElseThrow(() -> new Exception ("Store not found with id: " + id));
        tenantAccess.requireStoreAccess(userService.getCurrentUser(), store);

        return StoreMapper.toDTO(store);
    }

    @Override
    public List<StoreDTO> getAllStores() throws Exception {
        if (userService.getCurrentUser().getRole() != UserRole.ROLE_ADMIN) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only a system administrator can list all stores");
        }
        List<Store> dtos =  storeRepository.findAll();
        return dtos.stream().map(StoreMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public Store getStoreByAdmin() throws UserException {
        User admin = userService.getCurrentUser();
        return storeRepository.findByStoreAdminId(admin.getId());
    }

    @Override
    public StoreDTO updateStore(Long id, StoreDTO storeDto) throws UserException {

        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() == UserRole.ROLE_STORE_MANAGER
                || currentUser.getRole() == UserRole.ROLE_BRANCH_MANAGER
                || currentUser.getRole() == UserRole.ROLE_BRANCH_CASHIER) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only the store administrator or system administrator can update a store");
        }

        Store existing = storeRepository.findByStoreAdminId(currentUser.getId());

        if (existing == null) {
            throw new UserException("Store not found for the current user");
        }
        if (!existing.getId().equals(id)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You don't have permission to update this store");
        }

        existing.setBrand(storeDto.getBrand());
        existing.setDescription(storeDto.getDescription());

        if (storeDto.getStoreType() != null) {
            existing.setStoreType(storeDto.getStoreType());
        }

        if (storeDto.getContact() != null){
            StoreContact contact = StoreContact.builder()
                    .address(storeDto.getContact().getAddress())
                    .phone(storeDto.getContact().getPhone())
                    .email(storeDto.getContact().getEmail())
                    .build();
            existing.setContact(contact);
        }
        Store updatedStore = storeRepository.save(existing);
        return StoreMapper.toDTO(updatedStore);
    }

    @Override
    public void deleteStore(Long id) throws UserException {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new UserException("Store not found with id: " + id));
        tenantAccess.requireStoreAccess(userService.getCurrentUser(), store);
        if (userService.getCurrentUser().getRole() != UserRole.ROLE_STORE_ADMIN) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only the store administrator can delete a store");
        }
        storeRepository.delete(store);

    }

    @Override
    public StoreDTO getStoreByEmployee() throws UserException {

        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            throw new UserException("Don't have permission to access this resource");
        }
        return StoreMapper.toDTO(tenantAccess.requireActorStore(currentUser));
    }

    @Override
    public StoreDTO moderateStore(Long id, StoreStatus status) throws UserException {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() != UserRole.ROLE_ADMIN) {
            throw new UserException("Only a system admin can moderate stores");
        }

        Store store = storeRepository.findById(id).orElseThrow(() -> new UserException("Store not found with id: " + id));
        StoreStatus previousStatus = store.getStatus();
        store.setStatus(status);

        Store updatedStore = storeRepository.save(store);
        auditLogRepository.save(AdminAuditLog.builder()
                .actorId(currentUser.getId())
                .actorEmail(currentUser.getEmail())
                .action("STORE_STATUS_UPDATED")
                .resourceType("STORE")
                .resourceId(id.toString())
                .details("status: " + previousStatus + " -> " + status)
                .build());
        return StoreMapper.toDTO(updatedStore);
    }
}
