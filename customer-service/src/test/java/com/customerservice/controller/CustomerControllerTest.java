package com.customerservice.controller;

import com.customerservice.dto.CustomerRequestDTO;
import com.customerservice.dto.CustomerResponseDTO;
import com.customerservice.entity.EmployerType;
import com.customerservice.entity.UserType;
import com.customerservice.exception.EmailAlreadyExistsException;
import com.customerservice.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    private CustomerRequestDTO requestDTO;
    private CustomerResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = CustomerRequestDTO.builder()
                .firstName("John")
                .lastName("Smith")
                .dob(LocalDate.of(1990, 5, 15))
                .address("12 MG Road, Chennai")
                .contactNo("9876543210")
                .email("john.smith@example.com")
                .password("Str0ngPass!")
                .salary(BigDecimal.valueOf(800000))
                .panNo("ABCDE1234F")
                .employerType(EmployerType.SALARIED)
                .employerName("Acme Corp")
                .build();

        responseDTO = CustomerResponseDTO.builder()
                .customerId(1L)
                .firstName("John")
                .lastName("Smith")
                .dob(requestDTO.getDob())
                .address(requestDTO.getAddress())
                .contactNo(requestDTO.getContactNo())
                .email(requestDTO.getEmail())
                .salary(requestDTO.getSalary())
                .panNo(requestDTO.getPanNo())
                .employerType(EmployerType.SALARIED)
                .employerName("Acme Corp")
                .userType(UserType.D)
                .build();
    }

    @Test
    void registerCustomer_success() throws Exception {
        when(customerService.registerCustomer(any(CustomerRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1.0/customer/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.email").value("john.smith@example.com"));

        verify(customerService).registerCustomer(any(CustomerRequestDTO.class));
    }

    @Test
    void registerCustomer_validationError() throws Exception {
        requestDTO.setEmail("not-an-email");

        mockMvc.perform(post("/api/v1.0/customer/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerCustomer_emailAlreadyExists() throws Exception {
        when(customerService.registerCustomer(any(CustomerRequestDTO.class)))
                .thenThrow(EmailAlreadyExistsException.forEmail(requestDTO.getEmail()));

        mockMvc.perform(post("/api/v1.0/customer/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());
    }
}
