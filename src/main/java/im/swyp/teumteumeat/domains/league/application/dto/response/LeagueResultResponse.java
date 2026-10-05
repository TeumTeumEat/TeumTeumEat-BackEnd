package im.swyp.teumteumeat.domains.league.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record LeagueResultResponse(

        @Schema(description = "결과 대상 주의 시작일 (월요일)", example = "2026-09-28") LocalDate weekStartDate,

        @Schema(description = "최종 순위 (해당 주 스낵이 0개면 null)", example = "1", nullable = true) Integer rank,

        @Schema(description = "해당 주 스낵 수", example = "42") long weeklySnackCount
) {
}
