package com.cognizant.authservice.client;

import com.cognizant.authservice.dto.AuthUserResponse;
import com.cognizant.authservice.dto.CustomerAuthRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "customer-service")
public interface CustomerAuthClient {
    @PostMapping("/service/customer/verify")
    AuthUserResponse authenticate(@RequestBody CustomerAuthRequest request);
}
