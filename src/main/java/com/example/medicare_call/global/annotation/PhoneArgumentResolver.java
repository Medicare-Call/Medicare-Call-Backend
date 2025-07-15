package com.example.medicare_call.global.annotation;

import com.example.medicare_call.global.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;


//@AuthPhone이 붙은 파라미터에 실제 phone을 주입하는 로직
@Component
public class PhoneArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtProvider jwtProvider;

    public PhoneArgumentResolver(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    //이 파라미터에 값을 바인딩할 지 결정
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthPhone.class) &&
                parameter.getParameterType().equals(String.class);
    }

    //실제로 값을 꺼내서 컨트롤러의 파라미터로 넘겨주는 로직
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String token = extractTokenFromRequest(request);

        validateToken(token);

        return getPhoneFromToken(token);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new IllegalArgumentException("Authorization 헤더에 Bearer 토큰이 없습니다.");
    }

    private void validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("토큰이 존재하지 않습니다.");
        }

        if (!jwtProvider.verify(token)) {
            throw new IllegalArgumentException("유효하지 않은 전화번호 인증 토큰입니다.");
        }
    }

    private String getPhoneFromToken(String token) {
        return jwtProvider.getPhone(token);
    }
}
