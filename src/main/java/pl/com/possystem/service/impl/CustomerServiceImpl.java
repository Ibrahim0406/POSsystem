package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.com.possystem.entity.Customer;
import pl.com.possystem.repository.CustomerRepository;
import pl.com.possystem.repository.StoreRepository;
import pl.com.possystem.service.CustomerService;
import pl.com.possystem.service.TenantAccess;
import pl.com.possystem.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;
    private final UserService userService;


    @Override
    public Customer createCustomer(Customer customer) throws Exception {
        customer.setId(null);
        customer.setStore(TenantAccess.requireActorStore(userService.getCurrentUser(), storeRepository));
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Long id, Customer customerDetails) throws Exception {
        Customer customerToUpdate = customerRepository.findById(id)
                .orElseThrow(() -> new Exception("Customer not found with id: " + id));
        TenantAccess.requireCustomerAccess(userService.getCurrentUser(), customerToUpdate);


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
        TenantAccess.requireCustomerAccess(userService.getCurrentUser(), customerToDelete);

        customerRepository.delete(customerToDelete);
    }

    @Override
    public Customer getCustomerById(Long id) throws Exception {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new Exception("Customer not found with id: " + id));
        TenantAccess.requireCustomerAccess(userService.getCurrentUser(), customer);
        return customer;
    }

    @Override
    public List<Customer> getAllCustomers() throws Exception {
        return customerRepository.findByStoreId(currentStoreId());
    }

    @Override
    public List<Customer> searchCustomers(String keyword) throws Exception {
        Long storeId = currentStoreId();
        return customerRepository.searchByStoreIdAndKeyword(storeId, keyword);
    }

    private Long currentStoreId() throws pl.com.possystem.exception.UserException {
        return TenantAccess.requireActorStore(userService.getCurrentUser(), storeRepository).getId();
    }
}
