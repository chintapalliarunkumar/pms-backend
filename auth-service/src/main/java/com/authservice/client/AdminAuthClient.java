package com.authservice.client;

import com.authservice.dto.AdminAuthRequest;
import com.authservice.dto.AuthUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "policy-service")
public interface AdminAuthClient {
    @PostMapping("/service/admin/verify")
    AuthUserResponse authenticate(@RequestBody AdminAuthRequest request);
}
