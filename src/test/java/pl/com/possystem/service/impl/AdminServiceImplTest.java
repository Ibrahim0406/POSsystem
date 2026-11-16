package pl.com.possystem.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.entity.AdminAuditLog;
import pl.com.possystem.entity.Branch;
import pl.com.possystem.entity.Store;
import pl.com.possystem.entity.User;
import pl.com.possystem.exception.UserException;
import pl.com.possystem.payload.dto.AdminUserCreateDTO;
import pl.com.possystem.repository.*;
import pl.com.possystem.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminServiceImplTest {

    private StoreRepository storeRepository;
    private BranchRepository branchRepository;
    private UserRepository userRepository;
    private AdminAuditLogRepository auditLogRepository;
    private UserService userService;
    private PasswordEncoder passwordEncoder;
    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        storeRepository = mock(StoreRepository.class);
        branchRepository = mock(BranchRepository.class);
        userRepository = mock(UserRepository.class);
        auditLogRepository = mock(AdminAuditLogRepository.class);
        userService = mock(UserService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        adminService = new AdminServiceImpl(
                storeRepository,
                branchRepository,
                userRepository,
                mock(SupportReportRepository.class),
                auditLogRepository,
                mock(SystemSettingRepository.class),
                userService,
                passwordEncoder
        );
    }

    @Test
    void createsBranchUserWithEncodedPasswordAndAuditLog() throws Exception {
        Store store = new Store();
        store.setId(10L);
        Branch branch = new Branch();
        branch.setId(20L);
        branch.setStore(store);

        User actor = new User();
        actor.setId(1L);
        actor.setEmail("admin@example.com");
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));
        when(branchRepository.findById(20L)).thenReturn(Optional.of(branch));
        when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(userRepository.existsByFullNameIgnoreCase("New User")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(30L);
            return user;
        });
        when(userService.getCurrentUser()).thenReturn(actor);

        AdminUserCreateDTO request = new AdminUserCreateDTO();
        request.setFullName(" New User ");
        request.setEmail("NEW@example.com");
        request.setPassword("password");
        request.setRole(UserRole.ROLE_BRANCH_CASHIER);
        request.setStoreId(10L);
        request.setBranchId(20L);

        var result = adminService.createUser(request);

        assertEquals(30L, result.getId());
        assertEquals("new@example.com", result.getEmail());
        assertEquals(UserRole.ROLE_BRANCH_CASHIER, result.getRole());
        assertEquals(10L, result.getStoreId());
        assertEquals(20L, result.getBranchId());
        verify(userRepository).save(argThat(user ->
                "encoded-password".equals(user.getPassword())
                        && "New User".equals(user.getFullName())));
        verify(auditLogRepository).save(any(AdminAuditLog.class));
    }

    @Test
    void rejectsCreatingAnotherSystemAdministrator() {
        AdminUserCreateDTO request = new AdminUserCreateDTO();
        request.setFullName("Admin");
        request.setEmail("admin@example.com");
        request.setPassword("password");
        request.setRole(UserRole.ROLE_ADMIN);

        assertThrows(UserException.class, () -> adminService.createUser(request));

        verifyNoInteractions(userRepository, passwordEncoder, auditLogRepository);
    }
}
