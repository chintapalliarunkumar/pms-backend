package com.customerservice.controller;

import com.customerservice.dto.AuthUserResponseDTO;
import com.customerservice.dto.LoginRequestDTO;
import com.customerservice.exception.InvalidCredentialsException;
import com.customerservice.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CustomerCredentialController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerCredentialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Test
    void verify_success() throws Exception {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("john.smith@example.com")
                .password("Str0ngPass!")
                .build();

        when(customerService.verifyCredentials(any(LoginRequestDTO.class)))
                .thenReturn(new AuthUserResponseDTO("john.smith@example.com", "CUSTOMER"));

        mockMvc.perform(post("/service/customer/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john.smith@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void verify_invalidCredentials() throws Exception {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("john.smith@example.com")
                .password("wrong")
                .build();

        when(customerService.verifyCredentials(any(LoginRequestDTO.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/service/customer/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verify_validationError() throws Exception {
        LoginRequestDTO request = LoginRequestDTO.builder()
                .email("")
                .password("")
                .build();

        mockMvc.perform(post("/service/customer/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
