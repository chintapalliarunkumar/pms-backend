package com.policyservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.policyservice.dto.AdminLoginRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminCredentialController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "admin.default.username=admin",
        "admin.default.password=admin"
})
class AdminCredentialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void verify_success() throws Exception {
        AdminLoginRequestDTO request = AdminLoginRequestDTO.builder()
                .username("admin")
                .password("admin")
                .build();

        mockMvc.perform(post("/service/admin/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void verify_invalidCredentials() throws Exception {
        AdminLoginRequestDTO request = AdminLoginRequestDTO.builder()
                .username("admin")
                .password("wrongpass")
                .build();

        mockMvc.perform(post("/service/admin/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verify_validationError() throws Exception {
        AdminLoginRequestDTO request = AdminLoginRequestDTO.builder()
                .username("")
                .password("")
                .build();

        mockMvc.perform(post("/service/admin/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
