package com.authservice.client;

import com.authservice.dto.AuthUserResponse;
import com.authservice.dto.CustomerAuthRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "customer-service")
public interface CustomerAuthClient {
    @PostMapping("/service/customer/verify")
    AuthUserResponse authenticate(@RequestBody CustomerAuthRequest request);
}
