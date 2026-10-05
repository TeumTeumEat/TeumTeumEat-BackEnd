package im.swyp.teumteumeat.domains.league.presentation.controller;

import im.swyp.teumteumeat.domains.league.application.dto.response.LeagueResponse;
import im.swyp.teumteumeat.domains.league.application.dto.response.LeagueResultResponse;
import im.swyp.teumteumeat.domains.league.application.usecase.LeagueUseCase;
import im.swyp.teumteumeat.domains.league.presentation.api.LeagueApi;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.annotation.LoginUser;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/league")
@RequiredArgsConstructor
public class LeagueController implements LeagueApi {

    private final LeagueUseCase leagueUseCase;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<LeagueResponse>> getLeague(
            @LoginUser CustomUserDetails user) {
        LeagueResponse response = leagueUseCase.getLeague(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @GetMapping("/results/latest")
    public ResponseEntity<ApiResponse<LeagueResultResponse>> getLatestResult(
            @LoginUser CustomUserDetails user) {
        LeagueResultResponse response = leagueUseCase.getLatestResult(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }
}
