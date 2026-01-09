package com.example.medicare_call.global.annotation;

import com.example.medicare_call.global.jwt.JwtTokenAuthentication;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

//@AuthUser가 붙은 파라미터에 실제 memberId를 주입하는 로직
@Component
public class AuthenticationArgumentResolver implements HandlerMethodArgumentResolver {

    //이 파라미터에 값을 바인딩할 지 결정
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class) && //@AuthUser가 붙어있고
                parameter.getParameterType().equals(Integer.class); //타입이 Integer인 경우 resolver 동작
    }

    //실제고 값을 꺼내서 컨트롤러의 파라미터로 넘겨주는 로직
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); //SecurityContextHolder에서 현재 로그인한 사용자의 인증 정보를 꺼냄
        validateAuthentication(authentication); //인증 정보가 없거나 인증되지 않은 유저는 예외 발생

        return getUsIdxFromAuthentication(authentication);
    }

    private void validateAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("인증되지 않은 유저입니다.");
        }
    }

    private Integer getUsIdxFromAuthentication(Authentication authentication) {
        if (!(authentication instanceof JwtTokenAuthentication)) {
            throw new IllegalArgumentException("Authentication 설정이 잘못되었습니다."); //사용자 인증 객체가 JwtTokenAuthentication 타입인지 확인
        }
        return ((JwtTokenAuthentication) authentication).getUserId();
    }
}
