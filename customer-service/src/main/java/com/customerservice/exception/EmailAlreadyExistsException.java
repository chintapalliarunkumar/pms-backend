package com.customerservice.exception;

/**
 * Thrown when a customer registration is attempted with an email
 * that already exists in the system.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    public static EmailAlreadyExistsException forEmail(String email) {
        return new EmailAlreadyExistsException("Email already exists in the system: " + email);
    }
}
