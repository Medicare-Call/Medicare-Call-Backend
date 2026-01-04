package com.example.medicare_call.api;

import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.global.annotation.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Home", description = "홈 화면 관련 API")
public interface HomeApi {

    @Operation(
            summary = "홈 화면 데이터 조회",
            description = "보호자의 홈 화면에 필요한 어르신 상태 요약 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "홈 화면 데이터 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HomeReportResponse.class)
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
    ResponseEntity<HomeReportResponse> getHomeData(
            @Parameter(hidden = true) @AuthUser Long memberId,
            @Parameter(description = "조회할 어르신의 식별자", required = true, example = "1")
            @PathVariable("elderId") Integer elderId
    );
}
