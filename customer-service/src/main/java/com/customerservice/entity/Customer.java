package com.customerservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing a Customer registered in the Policy Management System.
 */
@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_email", columnNames = "email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "contact_no", nullable = false, length = 20)
    private String contactNo;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Stores the BCrypt-hashed password used for customer login (see
     * CustomerServiceImpl.login). Never the raw/plain-text password, and
     * deliberately excluded from CustomerResponseDTO so it's never
     * returned over the API.
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal salary;

    @Column(name = "pan_no", nullable = false, length = 10)
    private String panNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "employer_type", nullable = false, length = 20)
    private EmployerType employerType;

    @Column(name = "employer_name", length = 150)
    private String employerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 5)
    private UserType userType;
}
