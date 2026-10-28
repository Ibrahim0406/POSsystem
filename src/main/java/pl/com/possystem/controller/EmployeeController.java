package pl.com.possystem.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.entity.User;
import pl.com.possystem.payload.dto.UserDto;
import pl.com.possystem.payload.response.ApiResponse;
import pl.com.possystem.service.EmployeeService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

     @PostMapping("/store/{storeId}")
    public ResponseEntity<UserDto> createStoreEmployee(@RequestBody UserDto userDto,@PathVariable Long storeId) throws Exception {

        UserDto employee = employeeService.createStoreEmployee(userDto, storeId);

        return ResponseEntity.ok(employee);
    }


    @PostMapping("/branch/{branchId}")
    public ResponseEntity<UserDto> createBranchEmployee(@RequestBody UserDto userDto,@PathVariable Long branchId) throws Exception {

        UserDto employee = employeeService.createBranchEmployee(userDto, branchId);

        return ResponseEntity.ok(employee);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<User> updateEmployee(@PathVariable Long id,@RequestBody UserDto userDto) throws Exception {

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
    public ResponseEntity<List<UserDto>> getStoreEmployees(@PathVariable Long id, @RequestParam(required = false)UserRole userRole) throws Exception {

        List<UserDto> employees = employeeService.findStoreEmployees(id, userRole);

        return ResponseEntity.ok(employees);
    }

    @GetMapping("/branch/{id}")
    public ResponseEntity<List<UserDto>> getBranchEmployees(@PathVariable Long id, @RequestParam(required = false)UserRole userRole) throws Exception {

        List<UserDto> employees = employeeService.findBranchEmployees(id, userRole);

        return ResponseEntity.ok(employees);
    }


}


