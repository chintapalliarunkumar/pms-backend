package com.customerservice.entity;

/**
 * Represents the derived user category based on annual salary (in Lakhs).
 * A: salary <= 5L
 * B: salary <= 10L
 * C: salary <= 15L
 * D: salary <= 30L
 * E: salary > 30L
 */
public enum UserType {
    A,
    B,
    C,
    D,
    E
}
