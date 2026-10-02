package com.cognizant.authservice.client;

import com.cognizant.authservice.dto.AdminAuthRequest;
import com.cognizant.authservice.dto.AuthUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "policy-service")
public interface AdminAuthClient {
    @PostMapping("/service/admin/verify")
    AuthUserResponse authenticate(@RequestBody AdminAuthRequest request);
}
