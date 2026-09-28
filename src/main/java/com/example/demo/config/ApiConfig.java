package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;

@Configuration
public class ApiConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    OpenAPI bloodLinkOpenApi() {
        return new OpenAPI().info(new Info().title("BloodLink API")
                .description("Blood donor registry and search system")
                .version("1.0.0"));
    }

    @Bean
    OpenApiCustomizer commonErrorResponses() {
        return openApi -> openApi.getPaths().values().forEach(path -> path.readOperations().forEach(operation -> {
            operation.getResponses().putIfAbsent("400", new ApiResponse().description("Validation or business-rule error."));
            operation.getResponses().putIfAbsent("404", new ApiResponse().description("Requested resource does not exist."));
            operation.getResponses().putIfAbsent("409", new ApiResponse().description("Duplicate or relationship conflict."));
        }));
    }
}