package com.example.medicare_call.controller;

import com.example.medicare_call.dto.MemberInfoResponse;
import com.example.medicare_call.dto.MemberInfoUpdateRequest;
import com.example.medicare_call.dto.auth.MemberRegisterRequest;
import com.example.medicare_call.dto.auth.TokenResponse;
import com.example.medicare_call.global.annotation.AuthPhone;
import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.service.MemberService;
import com.example.medicare_call.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원가입/로그인/회원정보 API")
@RequiredArgsConstructor
@RestController
public class MemberController {

    private final AuthService authService;
    private final MemberService memberService;

    @PostMapping("/members")
    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다")
    public ResponseEntity<TokenResponse> register(@Parameter(hidden = true) @AuthPhone String phone,
                                           @Valid @RequestBody MemberRegisterRequest signUpDto) {
        TokenResponse tokenResponse = authService.register(phone,signUpDto);
        return ResponseEntity.ok(tokenResponse);
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다")
    public ResponseEntity<MemberInfoResponse> getMemberInfo(@Parameter(hidden = true) @AuthUser Long memberId){
        MemberInfoResponse memberInfoResponse = memberService.getMemberInfo(memberId.intValue());
        return ResponseEntity.ok(memberInfoResponse);
    }

    @PostMapping("/me")
    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다")
    public ResponseEntity<MemberInfoResponse> updateMemberInfo(@Parameter(hidden = true) @AuthUser Long memberId,
                                                               @Valid @RequestBody MemberInfoUpdateRequest request){
        MemberInfoResponse response = memberService.updateMemberInfo(memberId.intValue(), request);
        return ResponseEntity.ok(response);
    }
}
