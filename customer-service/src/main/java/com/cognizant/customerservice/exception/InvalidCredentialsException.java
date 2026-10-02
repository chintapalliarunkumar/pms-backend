package com.cognizant.customerservice.exception;

/**
 * Thrown when login credentials are invalid (unknown email or incorrect
 * password). Deliberately generic in its message to avoid revealing which
 * part of the credential pair was wrong.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
