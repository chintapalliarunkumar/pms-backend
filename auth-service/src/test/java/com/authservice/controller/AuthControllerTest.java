package com.authservice.controller;

import com.authservice.client.AdminAuthClient;
import com.authservice.client.CustomerAuthClient;
import com.authservice.dto.AdminAuthRequest;
import com.authservice.dto.AuthUserResponse;
import com.authservice.dto.CustomerAuthRequest;
import com.authservice.dto.LoginRequest;
import com.authservice.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerAuthClient customerAuthClient;

    @MockBean
    private AdminAuthClient adminAuthClient;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void customerLogin_success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("john.smith@example.com");
        request.setPassword("Str0ngPass!");

        when(customerAuthClient.authenticate(any(CustomerAuthRequest.class)))
                .thenReturn(new AuthUserResponse("john.smith@example.com", "CUSTOMER"));
        when(jwtUtil.generateToken("john.smith@example.com", "CUSTOMER")).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/api/v1.0/auth/customer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    void customerLogin_invalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("john.smith@example.com");
        request.setPassword("wrong");

        when(customerAuthClient.authenticate(any(CustomerAuthRequest.class)))
                .thenThrow(unauthorizedFeignException());

        mockMvc.perform(post("/api/v1.0/auth/customer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminLogin_success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin");

        when(adminAuthClient.authenticate(any(AdminAuthRequest.class)))
                .thenReturn(new AuthUserResponse("admin", "ADMIN"));
        when(jwtUtil.generateToken("admin", "ADMIN")).thenReturn("mocked-admin-token");

        mockMvc.perform(post("/api/v1.0/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-admin-token"));
    }

    @Test
    void adminLogin_invalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");

        when(adminAuthClient.authenticate(any(AdminAuthRequest.class)))
                .thenThrow(unauthorizedFeignException());

        mockMvc.perform(post("/api/v1.0/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerLogin_validationError() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("");

        mockMvc.perform(post("/api/v1.0/auth/customer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private FeignException.Unauthorized unauthorizedFeignException() {
        Request feignRequest = Request.create(Request.HttpMethod.POST, "/service/verify",
                Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());
        return new FeignException.Unauthorized("Unauthorized", feignRequest, null, null);
    }
}
