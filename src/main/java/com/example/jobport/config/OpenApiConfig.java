package com.example.jobport.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "JobPort API",
                version = "1.0",
                description = "Job portal backend APIs"
        )
)
public class OpenApiConfig {
}