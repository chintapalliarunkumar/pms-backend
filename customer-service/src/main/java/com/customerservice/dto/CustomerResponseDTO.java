package com.customerservice.dto;

import com.customerservice.entity.EmployerType;
import com.customerservice.entity.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response payload returned after customer registration or lookup.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Customer details returned by the API")
public class CustomerResponseDTO {

    private Long customerId;
    private String firstName;
    private String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    private String address;
    private String contactNo;
    private String email;
    private BigDecimal salary;
    private String panNo;
    private EmployerType employerType;
    private String employerName;
    private UserType userType;
}
