package com.cognizant.policyservice.exception;

/**
 * Thrown when admin login credentials are invalid (unknown username or
 * incorrect password). Deliberately generic to avoid revealing which
 * part of the credential pair was wrong.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
