package com.policyservice.exception;

/**
 * Thrown when policy registration data violates a business rule,
 * e.g. invalid start date, missing policy type / user types.
 */
public class InvalidPolicyDataException extends RuntimeException {

    public InvalidPolicyDataException(String message) {
        super(message);
    }
}
