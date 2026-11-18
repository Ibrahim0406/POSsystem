package pl.com.possystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.domain.StoreStatus;
import pl.com.possystem.entity.Branch;
import pl.com.possystem.entity.Customer;
import pl.com.possystem.entity.Store;
import pl.com.possystem.entity.User;
import pl.com.possystem.repository.StoreRepository;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TenantAccess {

    private final StoreRepository storeRepository;

    public void requireStoreAccess(User actor, Store store) {
        if (actor == null || store == null) {
            throw new AccessDeniedException("You don't have permission to access this store");
        }
        if (actor.getRole() == UserRole.ROLE_ADMIN) {
            return;
        }
        if (store.getStatus() != StoreStatus.ACTIVE) {
            throw new AccessDeniedException("This store is pending approval or is not active");
        }

        boolean isStoreAdmin = actor.getRole() == UserRole.ROLE_STORE_ADMIN
                && store.getStoreAdmin() != null
                && Objects.equals(actor.getId(), store.getStoreAdmin().getId());
        Store actorStore = getStore(actor);
        if (!isStoreAdmin && (actorStore == null || !Objects.equals(actorStore.getId(), store.getId()))) {
            throw new AccessDeniedException("You don't have permission to access this store");
        }
    }

    public void requireStoreManagement(User actor, Store store) {
        requireStoreAccess(actor, store);
        if (actor.getRole() != UserRole.ROLE_ADMIN
                && actor.getRole() != UserRole.ROLE_STORE_ADMIN
                && actor.getRole() != UserRole.ROLE_STORE_MANAGER) {
            throw new AccessDeniedException("You don't have permission to manage this store");
        }
    }

    public void requireBranchAccess(User actor, Branch branch) {
        if (branch == null || branch.getStore() == null) {
            throw new AccessDeniedException("You don't have permission to access this branch");
        }
        requireStoreAccess(actor, branch.getStore());
        if (actor.getRole() == UserRole.ROLE_BRANCH_MANAGER
                || actor.getRole() == UserRole.ROLE_BRANCH_CASHIER) {
            if (actor.getBranch() == null || !Objects.equals(actor.getBranch().getId(), branch.getId())) {
                throw new AccessDeniedException("You don't have permission to access this branch");
            }
        }
    }

    public void requireBranchManagement(User actor, Branch branch) {
        requireBranchAccess(actor, branch);
        if (actor.getRole() != UserRole.ROLE_ADMIN
                && actor.getRole() != UserRole.ROLE_STORE_ADMIN
                && actor.getRole() != UserRole.ROLE_STORE_MANAGER
                && actor.getRole() != UserRole.ROLE_BRANCH_MANAGER) {
            throw new AccessDeniedException("You don't have permission to manage this branch");
        }
    }

    public void requireUserAccess(User actor, User target) {
        if (actor == null || target == null) {
            throw new AccessDeniedException("You don't have permission to access this user");
        }
        if (actor.getRole() == UserRole.ROLE_ADMIN) {
            return;
        }
        if (actor.getRole() == UserRole.ROLE_BRANCH_CASHIER) {
            if (!Objects.equals(actor.getId(), target.getId())) {
                throw new AccessDeniedException("You don't have permission to access this user");
            }
            return;
        }
        Store store = getStore(target);
        if (store == null) {
            throw new AccessDeniedException("You don't have permission to access this user");
        }
        requireStoreAccess(actor, store);
        if ((actor.getRole() == UserRole.ROLE_BRANCH_MANAGER
                || actor.getRole() == UserRole.ROLE_BRANCH_CASHIER)
                && (target.getBranch() == null || actor.getBranch() == null
                || !Objects.equals(actor.getBranch().getId(), target.getBranch().getId())
                || target.getRole() != UserRole.ROLE_BRANCH_CASHIER)) {
            throw new AccessDeniedException("You don't have permission to access this user");
        }
    }

    public void requireUserManagement(User actor, User target) {
        requireUserAccess(actor, target);
        if (actor.getRole() == UserRole.ROLE_ADMIN
                || actor.getRole() == UserRole.ROLE_STORE_ADMIN
                || actor.getRole() == UserRole.ROLE_STORE_MANAGER) {
            return;
        }
        if (actor.getRole() == UserRole.ROLE_BRANCH_MANAGER
                && target.getRole() == UserRole.ROLE_BRANCH_CASHIER
                && actor.getBranch() != null
                && target.getBranch() != null
                && Objects.equals(actor.getBranch().getId(), target.getBranch().getId())) {
            return;
        }
        throw new AccessDeniedException("You don't have permission to manage this user");
    }

    public void requireCustomerAccess(User actor, Customer customer) {
        if (actor != null && actor.getRole() == UserRole.ROLE_ADMIN) {
            return;
        }
        requireStoreAccess(actor, customer == null ? null : customer.getStore());
    }

    public Store getStore(User user) {
        if (user == null) {
            return null;
        }
        if (user.getStore() != null) {
            return user.getStore();
        }
        return user.getBranch() == null ? null : user.getBranch().getStore();
    }

    public Store requireActorStore(User actor) {
        Store store = getStore(actor);
        if (store == null && actor != null && actor.getRole() == UserRole.ROLE_STORE_ADMIN) {
            store = storeRepository.findByStoreAdminId(actor.getId());
        }
        if (store == null) {
            throw new AccessDeniedException("The current user is not assigned to a store");
        }
        requireStoreAccess(actor, store);
        return store;
    }
}
