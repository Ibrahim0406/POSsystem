package pl.com.possystem.service;


import pl.com.possystem.domain.UserRole;
import pl.com.possystem.entity.User;
import pl.com.possystem.payload.dto.UserDto;

import java.util.List;

public interface EmployeeService {

    UserDto createStoreEmployee(UserDto employee, Long storeId) throws Exception;

    UserDto createBranchEmployee(UserDto employee, Long branchId) throws Exception;

    User updateEmployee(Long employeeId, UserDto employeeDetails) throws Exception;

    void deleteEmployee(Long employeeId) throws Exception;

    List<UserDto> findStoreEmployees(Long storeId, UserRole userrole) throws Exception;

    List<UserDto> findBranchEmployees(Long branchId, UserRole userrole) throws Exception;

}
