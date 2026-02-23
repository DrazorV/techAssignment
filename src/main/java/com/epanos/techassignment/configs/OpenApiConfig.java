package com.epanos.techassignment.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Match & Odds API", version = "1.0", description = "REST API for managing matches and their betting odds"))
public class OpenApiConfig {}
