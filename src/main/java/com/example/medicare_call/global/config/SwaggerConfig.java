package com.example.medicare_call.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class SwaggerConfig {

    private final String securitySchemaName = "JWT";

    private Info apiInfo() {
        return new Info()
                .title("Medicare Call API"); //나중에 info추가 가능
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(setComponents())
                .info(apiInfo())
                .addSecurityItem(setSecurityItems());
    }

    private Components setComponents() {
        return new Components()
                .addSecuritySchemes(securitySchemaName, bearerAuth());
    }

    private SecurityScheme bearerAuth() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("Bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name(HttpHeaders.AUTHORIZATION);
    }

    private SecurityRequirement setSecurityItems() {
        return new SecurityRequirement()
                .addList(securitySchemaName);
    }

}
