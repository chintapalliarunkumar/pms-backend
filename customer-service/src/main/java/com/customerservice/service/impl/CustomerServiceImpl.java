package com.customerservice.service.impl;

import com.customerservice.dto.CustomerRequestDTO;
import com.customerservice.dto.CustomerResponseDTO;
import com.customerservice.dto.LoginRequestDTO;
import com.customerservice.dto.AuthUserResponseDTO;
import com.customerservice.entity.Customer;
import com.customerservice.entity.EmployerType;
import com.customerservice.entity.UserType;
import com.customerservice.exception.EmailAlreadyExistsException;
import com.customerservice.exception.InvalidCredentialsException;
import com.customerservice.exception.InvalidEmployerDetailsException;
import com.customerservice.repository.CustomerRepository;
import com.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * Implementation of {@link CustomerService} handling customer registration and credential verification,
 * along with business rule validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private static final BigDecimal LAKH = BigDecimal.valueOf(100_000);
    private static final BigDecimal FIVE_LAKH = BigDecimal.valueOf(5).multiply(LAKH);
    private static final BigDecimal TEN_LAKH = BigDecimal.valueOf(10).multiply(LAKH);
    private static final BigDecimal FIFTEEN_LAKH = BigDecimal.valueOf(15).multiply(LAKH);
    private static final BigDecimal THIRTY_LAKH = BigDecimal.valueOf(30).multiply(LAKH);

    private static final String CUSTOMER_ROLE = "CUSTOMER";

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CustomerResponseDTO registerCustomer(CustomerRequestDTO requestDTO) {
        log.info("Registering customer with email: {}", requestDTO.getEmail());

        validateEmailUniqueness(requestDTO.getEmail());
        validateEmployerDetails(requestDTO.getEmployerType(), requestDTO.getEmployerName());

        UserType userType = calculateUserType(requestDTO.getSalary());

        Customer customer = Customer.builder()
                .firstName(requestDTO.getFirstName())
                .lastName(requestDTO.getLastName())
                .dob(requestDTO.getDob())
                .address(requestDTO.getAddress())
                .contactNo(requestDTO.getContactNo())
                .email(requestDTO.getEmail())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .salary(requestDTO.getSalary())
                .panNo(requestDTO.getPanNo())
                .employerType(requestDTO.getEmployerType())
                .employerName(requestDTO.getEmployerName())
                .userType(userType)
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer registered successfully with id: {}", savedCustomer.getCustomerId());

        return mapToResponseDTO(savedCustomer);
    }

    @Override
    public AuthUserResponseDTO verifyCredentials(LoginRequestDTO requestDTO) {
        Customer customer = customerRepository.findByEmail(requestDTO.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(requestDTO.getPassword(), customer.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        return new AuthUserResponseDTO(customer.getEmail(), CUSTOMER_ROLE);
    }

    private void validateEmailUniqueness(String email) {
        if (customerRepository.existsByEmail(email)) {
            throw EmailAlreadyExistsException.forEmail(email);
        }
    }

    private void validateEmployerDetails(EmployerType employerType, String employerName) {
        boolean employerNamePresent = StringUtils.hasText(employerName);

        if (employerType == EmployerType.SELF_EMPLOYED && employerNamePresent) {
            throw new InvalidEmployerDetailsException(
                    "Employer name should not be present when employer type is SELF_EMPLOYED");
        }

        if (employerType == EmployerType.SALARIED && !employerNamePresent) {
            throw new InvalidEmployerDetailsException(
                    "Employer name is mandatory when employer type is SALARIED");
        }
    }

    private UserType calculateUserType(BigDecimal annualSalary) {
        if (annualSalary.compareTo(FIVE_LAKH) <= 0) {
            return UserType.A;
        } else if (annualSalary.compareTo(TEN_LAKH) <= 0) {
            return UserType.B;
        } else if (annualSalary.compareTo(FIFTEEN_LAKH) <= 0) {
            return UserType.C;
        } else if (annualSalary.compareTo(THIRTY_LAKH) <= 0) {
            return UserType.D;
        } else {
            return UserType.E;
        }
    }

    private CustomerResponseDTO mapToResponseDTO(Customer customer) {
        return CustomerResponseDTO.builder()
                .customerId(customer.getCustomerId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .dob(customer.getDob())
                .address(customer.getAddress())
                .contactNo(customer.getContactNo())
                .email(customer.getEmail())
                .salary(customer.getSalary())
                .panNo(customer.getPanNo())
                .employerType(customer.getEmployerType())
                .employerName(customer.getEmployerName())
                .userType(customer.getUserType())
                .build();
    }
}
