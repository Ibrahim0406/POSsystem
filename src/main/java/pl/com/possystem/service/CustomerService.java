package pl.com.possystem.service;

import pl.com.possystem.entity.Customer;

import java.util.List;

public interface CustomerService {

    Customer createCustomer(Customer customer) throws Exception;

    Customer updateCustomer(Long id, Customer customerDetails) throws Exception;

    void deleteCustomer(Long id) throws Exception;

    Customer getCustomerById(Long id) throws Exception;

    List<Customer> getAllCustomers() throws Exception;

    List<Customer> searchCustomers(String keyword) throws Exception;
}
