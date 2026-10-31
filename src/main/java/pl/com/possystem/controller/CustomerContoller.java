package pl.com.possystem.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.possystem.entity.Customer;
import pl.com.possystem.payload.response.ApiResponse;
import pl.com.possystem.service.CustomerService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerContoller {

    private final CustomerService customerService;


    @PostMapping("/create")
    public ResponseEntity<Customer> create(@RequestBody Customer customer) throws Exception {
            return ResponseEntity.ok(customerService.createCustomer(customer));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Customer> update(@PathVariable Long id, @RequestBody Customer customer) throws Exception {
        return ResponseEntity.ok(customerService.updateCustomer(id, customer));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) throws Exception {
        customerService.deleteCustomer(id);
        ApiResponse response = new ApiResponse();
        response.setMessage("Customer deleted successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping()
    public ResponseEntity<List<Customer>> getAll() throws Exception {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Customer>> search(@RequestParam String keyword) throws Exception {
        return ResponseEntity.ok(customerService.searchCustomers(keyword));
    }

}
