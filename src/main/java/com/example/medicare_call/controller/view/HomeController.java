package com.example.medicare_call.controller.view;

import com.example.medicare_call.dto.HomeResponse;
import com.example.medicare_call.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/elders")
@RequiredArgsConstructor
@Tag(name = "Home", description = "홈 화면 데이터 조회 API")
public class HomeController {

    private final HomeService homeService;

    @Operation(
        summary = "홈 화면 데이터 조회",
        description = "초기 화면 렌더링에 필요한 데이터를 제공하는 API입니다. 홈 화면 진입 시에 호출하면 됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "홈 화면 데이터 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = HomeResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "어르신 정보를 찾을 수 없음"
        )
    })
    @GetMapping("/{elderId}/home")
    public ResponseEntity<HomeResponse> getHomeData(
        @Parameter(description = "조회할 어르신의 식별자", required = true, example = "1")
        @PathVariable("elderId") Integer elderId
    ) {
        log.info("홈 화면 데이터 조회 요청: elderId={}", elderId);

        HomeResponse response = homeService.getHomeData(elderId);

        return ResponseEntity.ok(response);
    }
} 