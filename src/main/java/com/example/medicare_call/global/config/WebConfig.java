package com.example.medicare_call.global.config;

import com.example.medicare_call.global.annotation.AuthenticationArgumentResolver;
import com.example.medicare_call.global.annotation.PhoneArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthenticationArgumentResolver jwtHandlerArgumentResolver;
    private final PhoneArgumentResolver phoneArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(jwtHandlerArgumentResolver);
        resolvers.add(phoneArgumentResolver);
    }
}
