package com.customerservice.exception;

/**
 * Thrown when employer details violate business rules:
 * - SELF_EMPLOYED must not have an employer name.
 * - SALARIED must have an employer name.
 */
public class InvalidEmployerDetailsException extends RuntimeException {

    public InvalidEmployerDetailsException(String message) {
        super(message);
    }
}
