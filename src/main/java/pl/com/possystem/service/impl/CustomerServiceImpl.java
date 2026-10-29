package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.com.possystem.entity.Customer;
import pl.com.possystem.repository.CustomerRepository;
import pl.com.possystem.service.CustomerService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;


    @Override
    public Customer createCustomer(Customer customer) throws Exception {
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Long id, Customer customerDetails) throws Exception {
        Customer customerToUpdate = customerRepository.findById(id)
                .orElseThrow(() -> new Exception("Customer not found with id: " + id));


        customerToUpdate.setFullName(customerDetails.getFullName());
        customerToUpdate.setPhone(customerDetails.getPhone());
        customerToUpdate.setEmail(customerDetails.getEmail());
        customerToUpdate.setUpdatedAt(LocalDateTime.now());

        return customerRepository.save(customerToUpdate);
    }

    @Override
    public void deleteCustomer(Long id) throws Exception {
        Customer customerToDelete = customerRepository.findById(id)
                .orElseThrow(() -> new Exception("Customer not found with id: " + id));

        customerRepository.delete(customerToDelete);
    }

    @Override
    public Customer getCustomerById(Long id) throws Exception {
        return customerRepository.findById(id)
                .orElseThrow(() -> new Exception("Customer not found with id: " + id));
    }

    @Override
    public List<Customer> getAllCustomers() throws Exception {
        return customerRepository.findAll();
    }

    @Override
    public List<Customer> searchCustomers(String keyword) throws Exception {
        return customerRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
    }
}
