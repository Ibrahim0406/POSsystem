package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.domain.AccountStatus;
import pl.com.possystem.entity.Branch;
import pl.com.possystem.entity.Store;
import pl.com.possystem.entity.User;
import pl.com.possystem.mapper.UserMapper;
import pl.com.possystem.payload.dto.UserDTO;
import pl.com.possystem.exception.UserException;
import pl.com.possystem.repository.BranchRepository;
import pl.com.possystem.repository.StoreRepository;
import pl.com.possystem.repository.UserRepository;
import pl.com.possystem.service.EmployeeService;
import pl.com.possystem.service.UserService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final UserRepository userRepository;

    private final StoreRepository storeRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Override
    public UserDTO createStoreEmployee(UserDTO employee, Long storeId) throws Exception {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new Exception("Store not found"));
        User actor = userService.getCurrentUser();
        authorizeStore(actor, store);

        if (employee.getRole() != UserRole.ROLE_STORE_MANAGER
                && employee.getRole() != UserRole.ROLE_BRANCH_MANAGER) {
            throw new UserException("You don't have permission to assign this role");
        }
        if (employee.getRole() == UserRole.ROLE_STORE_MANAGER
                && actor.getRole() != UserRole.ROLE_STORE_ADMIN
                && actor.getRole() != UserRole.ROLE_ADMIN) {
            throw new UserException("Only a store admin can create a store manager");
        }
        requirePassword(employee.getPassword());

        Branch branch = null;

        if (employee.getRole() == UserRole.ROLE_BRANCH_MANAGER){
            if (employee.getBranchId() == null){
                throw new Exception("Branch ID is required for branch manager");
            }
            branch = branchRepository.findById(employee.getBranchId()).orElseThrow(() -> new Exception("Branch not found"));
            if (branch.getStore() == null || !Objects.equals(branch.getStore().getId(), store.getId())) {
                throw new UserException("Branch does not belong to this store");
            }
        }
        User user = UserMapper.toEntity(employee);
        user.setStore(store);
        user.setBranch(branch);
        user.setStatus(AccountStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode(employee.getPassword()));

        User savedEmployee = userRepository.save(user);

        if (employee.getRole() == UserRole.ROLE_BRANCH_MANAGER && branch != null) {
            branch.setManager(savedEmployee);
            branchRepository.save(branch);
        }

        return UserMapper.toDTO(savedEmployee);
    }

    @Override
    public UserDTO createBranchEmployee(UserDTO employee, Long branchId) throws Exception {

        Branch branch = branchRepository.findById(branchId).orElseThrow(() -> new Exception("Branch not found"));
        User actor = userService.getCurrentUser();
        authorizeBranch(actor, branch);

        if (employee.getRole() != UserRole.ROLE_BRANCH_CASHIER
                && employee.getRole() != UserRole.ROLE_BRANCH_MANAGER) {
            throw new UserException("You don't have permission to assign this role");
        }
        if (employee.getRole() == UserRole.ROLE_BRANCH_MANAGER
                && actor.getRole() != UserRole.ROLE_STORE_ADMIN
                && actor.getRole() != UserRole.ROLE_STORE_MANAGER
                && actor.getRole() != UserRole.ROLE_ADMIN) {
            throw new UserException("Only store management can create a branch manager");
        }
        requirePassword(employee.getPassword());

        User user = UserMapper.toEntity(employee);
        user.setStore(branch.getStore());
        user.setBranch(branch);
        user.setStatus(AccountStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode(employee.getPassword()));

        User savedEmployee = userRepository.save(user);
        if (employee.getRole() == UserRole.ROLE_BRANCH_MANAGER) {
            branch.setManager(savedEmployee);
            branchRepository.save(branch);
        }
        return UserMapper.toDTO(savedEmployee);
    }

    @Override
    public UserDTO updateEmployee(Long employeeId, UserDTO employeeDetails) throws Exception {

        User existingEmployee = userRepository.findById(employeeId).orElseThrow(() -> new Exception("Employee not found"));
        User actor = userService.getCurrentUser();
        authorizeEmployee(actor, existingEmployee);
        rejectPrivilegedEmployee(existingEmployee);

        if (employeeDetails.getRole() == null
                || employeeDetails.getRole() == UserRole.ROLE_ADMIN
                || employeeDetails.getRole() == UserRole.ROLE_STORE_ADMIN) {
            throw new UserException("You don't have permission to assign this role");
        }
        if (actor.getRole() == UserRole.ROLE_BRANCH_MANAGER
                && employeeDetails.getRole() != UserRole.ROLE_BRANCH_CASHIER) {
            throw new UserException("Branch managers can only manage cashiers");
        }
        if (actor.getRole() == UserRole.ROLE_STORE_MANAGER
                && employeeDetails.getRole() == UserRole.ROLE_STORE_MANAGER) {
            throw new UserException("Only a store admin can assign the store manager role");
        }

        Branch branch = null;
        if (employeeDetails.getBranchId() != null) {
            branch = branchRepository.findById(employeeDetails.getBranchId())
                    .orElseThrow(() -> new Exception("Branch not found"));
            Store employeeStore = getEmployeeStore(existingEmployee);
            if (employeeStore == null || branch.getStore() == null
                    || !Objects.equals(employeeStore.getId(), branch.getStore().getId())) {
                throw new UserException("Branch does not belong to the employee's store");
            }
            if (actor.getRole() == UserRole.ROLE_BRANCH_MANAGER
                    && (actor.getBranch() == null
                    || !Objects.equals(actor.getBranch().getId(), branch.getId()))) {
                throw new UserException("Branch managers cannot move employees to another branch");
            }
        } else if (employeeDetails.getRole() != UserRole.ROLE_STORE_MANAGER) {
            throw new UserException("Branch ID is required for this employee role");
        }

        existingEmployee.setEmail(employeeDetails.getEmail());
        existingEmployee.setFullName(employeeDetails.getFullName());
        existingEmployee.setPhone(employeeDetails.getPhone());
        if (employeeDetails.getPassword() != null && !employeeDetails.getPassword().isBlank()) {
            existingEmployee.setPassword(passwordEncoder.encode(employeeDetails.getPassword()));
        }
        existingEmployee.setRole(employeeDetails.getRole());
        existingEmployee.setBranch(branch);

        User updatedEmployee = userRepository.save(existingEmployee);
        if (updatedEmployee.getRole() == UserRole.ROLE_BRANCH_MANAGER && branch != null) {
            branch.setManager(updatedEmployee);
            branchRepository.save(branch);
        }
        return UserMapper.toDTO(updatedEmployee);
    }

    @Override
    public void deleteEmployee(Long employeeId) throws Exception {
        User employee = userRepository.findById(employeeId).orElseThrow(() -> new Exception("Employee not found"));
        User actor = userService.getCurrentUser();
        authorizeEmployee(actor, employee);
        rejectPrivilegedEmployee(employee);
        userRepository.delete(employee);

    }

    @Override
    public List<UserDTO> findStoreEmployees(Long storeId, UserRole userrole) throws Exception {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new Exception("Store not found"));
        authorizeStore(userService.getCurrentUser(), store);

        return userRepository.findByStore(store).stream()
                .filter(user -> userrole == null || user.getRole() == userrole)
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());

    }

    @Override
    public List<UserDTO> findBranchEmployees(Long branchId, UserRole userrole) throws Exception {
        Branch branch = branchRepository.findById(branchId).orElseThrow(() -> new Exception("Branch not found"));
        authorizeBranch(userService.getCurrentUser(), branch);

        return userRepository.findByBranchId(branchId).stream()
                .filter( user -> userrole == null || user.getRole() == userrole)
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());

    }

    private void authorizeStore(User actor, Store store) throws UserException {
        if (actor.getRole() == UserRole.ROLE_ADMIN) {
            return;
        }
        boolean isStoreAdmin = actor.getRole() == UserRole.ROLE_STORE_ADMIN
                && store.getStoreAdmin() != null
                && Objects.equals(actor.getId(), store.getStoreAdmin().getId());
        boolean isStoreManager = actor.getRole() == UserRole.ROLE_STORE_MANAGER
                && actor.getStore() != null
                && Objects.equals(actor.getStore().getId(), store.getId());
        if (!isStoreAdmin && !isStoreManager) {
            throw new UserException("You don't have permission to access this store");
        }
    }

    private void authorizeBranch(User actor, Branch branch) throws UserException {
        if (actor.getRole() == UserRole.ROLE_ADMIN) {
            return;
        }
        boolean managesStore = (actor.getRole() == UserRole.ROLE_STORE_ADMIN
                && branch.getStore() != null
                && branch.getStore().getStoreAdmin() != null
                && Objects.equals(actor.getId(), branch.getStore().getStoreAdmin().getId()))
                || (actor.getRole() == UserRole.ROLE_STORE_MANAGER
                && branch.getStore() != null
                && actor.getStore() != null
                && Objects.equals(actor.getStore().getId(), branch.getStore().getId()));
        boolean managesBranch = actor.getRole() == UserRole.ROLE_BRANCH_MANAGER
                && actor.getBranch() != null
                && Objects.equals(actor.getBranch().getId(), branch.getId());
        if (!managesStore && !managesBranch) {
            throw new UserException("You don't have permission to access this branch");
        }
    }

    private void authorizeEmployee(User actor, User employee) throws UserException {
        Store store = getEmployeeStore(employee);
        if (store == null) {
            throw new UserException("Employee is not assigned to a store");
        }
        if (actor.getRole() == UserRole.ROLE_BRANCH_MANAGER) {
            if (actor.getBranch() == null || employee.getBranch() == null
                    || !Objects.equals(actor.getBranch().getId(), employee.getBranch().getId())
                    || employee.getRole() != UserRole.ROLE_BRANCH_CASHIER) {
                throw new UserException("You don't have permission to access this employee");
            }
            return;
        }
        authorizeStore(actor, store);
    }

    private Store getEmployeeStore(User employee) {
        if (employee.getStore() != null) {
            return employee.getStore();
        }
        return employee.getBranch() != null ? employee.getBranch().getStore() : null;
    }

    private void rejectPrivilegedEmployee(User employee) throws UserException {
        if (employee.getRole() == UserRole.ROLE_ADMIN || employee.getRole() == UserRole.ROLE_STORE_ADMIN) {
            throw new UserException("Privileged accounts cannot be modified through employee management");
        }
    }

    private void requirePassword(String password) throws UserException {
        if (password == null || password.isBlank()) {
            throw new UserException("Password is required");
        }
    }

}
