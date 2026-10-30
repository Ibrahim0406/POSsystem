package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.entity.Branch;
import pl.com.possystem.entity.Store;
import pl.com.possystem.entity.User;
import pl.com.possystem.mapper.UserMapper;
import pl.com.possystem.payload.dto.UserDTO;
import pl.com.possystem.repository.BranchRepository;
import pl.com.possystem.repository.StoreRepository;
import pl.com.possystem.repository.UserRepository;
import pl.com.possystem.service.EmployeeService;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final UserRepository userRepository;

    private final StoreRepository storeRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO createStoreEmployee(UserDTO employee, Long storeId) throws Exception {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new Exception("Store not found"));

        Branch branch = null;

        if (employee.getRole() == UserRole.ROLE_BRANCH_MANAGER){
            if (employee.getBranchId() == null){
                throw new Exception("Branch ID is required for branch manager");
            }
            branch = branchRepository.findById(employee.getBranchId()).orElseThrow(() -> new Exception("Branch not found"));
        }
        User user = UserMapper.toEntity(employee);
        user.setStore(store);
        user.setBranch(branch);
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

          if (employee.getRole() == UserRole.ROLE_BRANCH_CASHIER || employee.getRole() == UserRole.ROLE_BRANCH_MANAGER) {
              User user = UserMapper.toEntity(employee);
              user.setBranch(branch);
              user.setPassword(passwordEncoder.encode(employee.getPassword()));

              User savedEmployee = userRepository.save(user);
              return UserMapper.toDTO(savedEmployee);
          }

        throw new Exception("Branch not found");
    }

    @Override
    public User updateEmployee(Long employeeId, UserDTO employeeDetails) throws Exception {

        User existingEmployee = userRepository.findById(employeeId).orElseThrow(() -> new Exception("Employee not found"));

        Branch branch = branchRepository.findById(employeeDetails.getBranchId()).orElseThrow(() -> new Exception("Branch not found"));

        existingEmployee.setEmail(employeeDetails.getEmail());
        existingEmployee.setFullName(employeeDetails.getFullName());
        existingEmployee.setPhone(employeeDetails.getPhone());
        existingEmployee.setPassword(employeeDetails.getPassword());
        existingEmployee.setRole(employeeDetails.getRole());
        existingEmployee.setBranch(branch);

        return userRepository.save(existingEmployee);
    }

    @Override
    public void deleteEmployee(Long employeeId) throws Exception {
        User employee = userRepository.findById(employeeId).orElseThrow(() -> new Exception("Employee not found"));
        userRepository.delete(employee);

    }

    @Override
    public List<UserDTO> findStoreEmployees(Long storeId, UserRole userrole) throws Exception {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new Exception("Store not found"));

        return userRepository.findByStore(store).stream()
                .filter(user -> userrole == null || user.getRole() == userrole)
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());

    }

    @Override
    public List<UserDTO> findBranchEmployees(Long branchId, UserRole userrole) throws Exception {
        Branch branch = branchRepository.findById(branchId).orElseThrow(() -> new Exception("Branch not found"));

        return userRepository.findByBranchId(branchId).stream()
                .filter( user -> userrole == null || user.getRole() == userrole)
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());

    }


}
