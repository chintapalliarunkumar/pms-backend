package com.cognizant.customerservice.repository;

import com.cognizant.customerservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for Customer entity operations.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);

    Optional<Customer> findByEmail(String email);
}
