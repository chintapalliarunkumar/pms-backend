package com.customerservice.exception;

/**
 * Thrown when a customer cannot be located by the given identifier.
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }

    public static CustomerNotFoundException forId(Long customerId) {
        return new CustomerNotFoundException("Customer not found with id: " + customerId);
    }
}
