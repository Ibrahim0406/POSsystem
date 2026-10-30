package pl.com.possystem.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.entity.User;
import pl.com.possystem.payload.dto.UserDTO;
import pl.com.possystem.payload.response.ApiResponse;
import pl.com.possystem.service.EmployeeService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

     @PostMapping("/store/{storeId}")
    public ResponseEntity<UserDTO> createStoreEmployee(@RequestBody UserDTO userDto, @PathVariable Long storeId) throws Exception {

        UserDTO employee = employeeService.createStoreEmployee(userDto, storeId);

        return ResponseEntity.ok(employee);
    }


    @PostMapping("/branch/{branchId}")
    public ResponseEntity<UserDTO> createBranchEmployee(@RequestBody UserDTO userDto, @PathVariable Long branchId) throws Exception {

        UserDTO employee = employeeService.createBranchEmployee(userDto, branchId);

        return ResponseEntity.ok(employee);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<User> updateEmployee(@PathVariable Long id,@RequestBody UserDTO userDto) throws Exception {

        User employee = employeeService.updateEmployee(id, userDto);

        return ResponseEntity.ok(employee);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteEmployee(@PathVariable Long id) throws Exception {

        employeeService.deleteEmployee(id);

        ApiResponse response = new ApiResponse();
        response.setMessage("Employee deleted successfully");

        return ResponseEntity.ok(response);
    }


    @GetMapping("/store/{id}")
    public ResponseEntity<List<UserDTO>> getStoreEmployees(@PathVariable Long id, @RequestParam(required = false)UserRole userRole) throws Exception {

        List<UserDTO> employees = employeeService.findStoreEmployees(id, userRole);

        return ResponseEntity.ok(employees);
    }

    @GetMapping("/branch/{id}")
    public ResponseEntity<List<UserDTO>> getBranchEmployees(@PathVariable Long id, @RequestParam(required = false)UserRole userRole) throws Exception {

        List<UserDTO> employees = employeeService.findBranchEmployees(id, userRole);

        return ResponseEntity.ok(employees);
    }


}


