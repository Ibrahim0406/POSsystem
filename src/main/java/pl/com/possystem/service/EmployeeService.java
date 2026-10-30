package pl.com.possystem.service;


import pl.com.possystem.domain.UserRole;
import pl.com.possystem.entity.User;
import pl.com.possystem.payload.dto.UserDTO;

import java.util.List;

public interface EmployeeService {

    UserDTO createStoreEmployee(UserDTO employee, Long storeId) throws Exception;

    UserDTO createBranchEmployee(UserDTO employee, Long branchId) throws Exception;

    User updateEmployee(Long employeeId, UserDTO employeeDetails) throws Exception;

    void deleteEmployee(Long employeeId) throws Exception;

    List<UserDTO> findStoreEmployees(Long storeId, UserRole userrole) throws Exception;

    List<UserDTO> findBranchEmployees(Long branchId, UserRole userrole) throws Exception;

}
