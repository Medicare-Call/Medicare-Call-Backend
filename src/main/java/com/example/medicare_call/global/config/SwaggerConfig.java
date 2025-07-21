package com.example.medicare_call.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private final String ACCESS_TOKEN_SCHEME = "AccessToken";
    private final String PHONE_TOKEN_SCHEME = "PhoneToken";
    private final String securitySchemaName = "JWT";

    private Info apiInfo() {
        return new Info()
                .title("Medicare Call API")
                .description("Access Token: 기존 회원용 토큰\nPhone Token: 신규 회원 인증용 토큰");
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(setComponents())
                .info(apiInfo())
                .servers(setServers())
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

    private List<Server> setServers() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080/api");
        localServer.setDescription("로컬 서버");

        Server httpsServer = new Server();
        httpsServer.setUrl("https://medicare-call.shop/api");
        httpsServer.setDescription("HTTPS 서버");

        return List.of(httpsServer, localServer);
    }
}
