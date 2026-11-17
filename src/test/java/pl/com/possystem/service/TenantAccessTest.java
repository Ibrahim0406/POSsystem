package pl.com.possystem.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.entity.Branch;
import pl.com.possystem.entity.Store;
import pl.com.possystem.entity.User;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TenantAccessTest {

    @Test
    void deniesUserFromAnotherStore() {
        Store currentStore = store(1L);
        Store otherStore = store(2L);
        User cashier = user(UserRole.ROLE_BRANCH_CASHIER, currentStore, branch(11L, currentStore));
        Branch otherBranch = branch(22L, otherStore);

        assertThrows(AccessDeniedException.class,
                () -> TenantAccess.requireBranchAccess(cashier, otherBranch));
    }

    @Test
    void limitsBranchStaffToTheirAssignedBranch() {
        Store store = store(1L);
        User cashier = user(UserRole.ROLE_BRANCH_CASHIER, store, branch(11L, store));

        assertThrows(AccessDeniedException.class,
                () -> TenantAccess.requireBranchAccess(cashier, branch(12L, store)));
    }

    @Test
    void permitsStaffToAccessTheirOwnTenantAndBranch() {
        Store store = store(1L);
        Branch branch = branch(11L, store);
        User cashier = user(UserRole.ROLE_BRANCH_CASHIER, store, branch);

        assertDoesNotThrow(() -> TenantAccess.requireBranchAccess(cashier, branch));
    }

    private Store store(Long id) {
        Store store = new Store();
        store.setId(id);
        return store;
    }

    private Branch branch(Long id, Store store) {
        Branch branch = new Branch();
        branch.setId(id);
        branch.setStore(store);
        return branch;
    }

    private User user(UserRole role, Store store, Branch branch) {
        User user = new User();
        user.setId(5L);
        user.setRole(role);
        user.setStore(store);
        user.setBranch(branch);
        return user;
    }
}
