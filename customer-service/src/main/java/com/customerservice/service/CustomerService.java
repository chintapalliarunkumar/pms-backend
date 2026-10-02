package com.customerservice.service;

import com.customerservice.dto.CustomerRequestDTO;
import com.customerservice.dto.CustomerResponseDTO;
import com.customerservice.dto.LoginRequestDTO;
import com.customerservice.dto.AuthUserResponseDTO;

/** Service contract for customer-related business operations. */
public interface CustomerService {
    CustomerResponseDTO registerCustomer(CustomerRequestDTO requestDTO);
    AuthUserResponseDTO verifyCredentials(LoginRequestDTO requestDTO);
}
