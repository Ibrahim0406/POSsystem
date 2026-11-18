package pl.com.possystem.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.entity.Store;
import pl.com.possystem.entity.User;
import pl.com.possystem.repository.AdminAuditLogRepository;
import pl.com.possystem.repository.StoreRepository;
import pl.com.possystem.repository.UserRepository;
import pl.com.possystem.payload.dto.StoreDTO;
import pl.com.possystem.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StoreServiceImplTest {

    @Test
    void allowsUnassignedStoreAdminToCreateStore() {
        StoreRepository storeRepository = mock(StoreRepository.class);
        AdminAuditLogRepository auditLogRepository = mock(AdminAuditLogRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = mock(UserService.class);
        StoreServiceImpl storeService = new StoreServiceImpl(
                storeRepository, auditLogRepository, userService, userRepository);
        User admin = new User();
        admin.setId(1L);
        admin.setRole(UserRole.ROLE_STORE_ADMIN);

        when(storeRepository.findByStoreAdminId(admin.getId())).thenReturn(null);
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> {
            Store store = invocation.getArgument(0);
            store.setId(10L);
            return store;
        });

        StoreDTO result = storeService.createStore(new StoreDTO(), admin);

        assertEquals(10L, result.getId());
        assertEquals(admin.getId(), result.getStoreAdmin().getId());
        assertEquals(UserRole.ROLE_STORE_ADMIN, result.getStoreAdmin().getRole());
        verify(userRepository).save(admin);
        verify(storeRepository).save(any(Store.class));
    }

    @Test
    void allowsSystemAdminToCreateStoreForUnassignedStoreAdmin() {
        StoreRepository storeRepository = mock(StoreRepository.class);
        AdminAuditLogRepository auditLogRepository = mock(AdminAuditLogRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = mock(UserService.class);
        StoreServiceImpl storeService = new StoreServiceImpl(
                storeRepository, auditLogRepository, userService, userRepository);
        User systemAdmin = new User();
        systemAdmin.setId(1L);
        systemAdmin.setRole(UserRole.ROLE_ADMIN);
        User storeAdmin = new User();
        storeAdmin.setId(2L);
        storeAdmin.setRole(UserRole.ROLE_STORE_ADMIN);
        StoreDTO request = new StoreDTO();
        pl.com.possystem.payload.dto.UserDTO storeAdminDTO = new pl.com.possystem.payload.dto.UserDTO();
        storeAdminDTO.setId(storeAdmin.getId());
        request.setStoreAdmin(storeAdminDTO);

        when(userRepository.findById(storeAdmin.getId())).thenReturn(java.util.Optional.of(storeAdmin));
        when(storeRepository.findByStoreAdminId(storeAdmin.getId())).thenReturn(null);
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> {
            Store store = invocation.getArgument(0);
            store.setId(11L);
            return store;
        });

        StoreDTO result = storeService.createStore(request, systemAdmin);

        assertEquals(11L, result.getId());
        assertEquals(storeAdmin.getId(), result.getStoreAdmin().getId());
        verify(userRepository).save(storeAdmin);
        assertEquals(UserRole.ROLE_STORE_ADMIN, storeAdmin.getRole());
    }

    @Test
    void deniesCashierFromCreatingStore() {
        StoreServiceImpl storeService = new StoreServiceImpl(
                mock(StoreRepository.class),
                mock(AdminAuditLogRepository.class),
                mock(UserService.class),
                mock(UserRepository.class));
        User cashier = new User();
        cashier.setRole(UserRole.ROLE_BRANCH_CASHIER);

        assertThrows(AccessDeniedException.class,
                () -> storeService.createStore(new StoreDTO(), cashier));
    }
}
