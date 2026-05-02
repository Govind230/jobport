package com.example.jobport.dto;

import com.example.jobport.entity.Role;
import lombok.*;

@Data
@Builder
public class RegisterResponse {
    private Long id;
    private String username;
    private String email;
    private Role role;
}
