package im.swyp.teumteumeat.domains.league.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record LeagueMyRankResponse(

        @Schema(description = "내 순위 (이번 주 스낵이 0개면 null)", example = "15", nullable = true) Integer rank,

        @Schema(description = "마스킹된 닉네임", example = "김*민") String name,

        @Schema(description = "이번 주 스낵 수", example = "12") long weeklySnackCount,

        @Schema(description = "오늘 스낵 수", example = "3") long todaySnackCount
) {
}
