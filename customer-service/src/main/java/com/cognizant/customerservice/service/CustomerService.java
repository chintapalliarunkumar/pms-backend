package com.cognizant.customerservice.service;

import com.cognizant.customerservice.dto.CustomerRequestDTO;
import com.cognizant.customerservice.dto.CustomerResponseDTO;
import com.cognizant.customerservice.dto.LoginRequestDTO;
import com.cognizant.customerservice.dto.AuthUserResponseDTO;

/** Service contract for customer-related business operations. */
public interface CustomerService {
    CustomerResponseDTO registerCustomer(CustomerRequestDTO requestDTO);
    AuthUserResponseDTO verifyCredentials(LoginRequestDTO requestDTO);
}
