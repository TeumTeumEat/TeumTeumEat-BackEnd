package im.swyp.teumteumeat.domains.league.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record LeagueResponse(

        @Schema(description = "이번 주 시작일 (월요일)", example = "2026-10-05") LocalDate weekStartDate,

        @Schema(description = "리그 리셋 시각 (KST, 다음 주 월요일 00:00)", example = "2026-10-12T00:00:00") LocalDateTime resetAt,

        @Schema(description = "리셋까지 남은 시간 (초)", example = "86400") long remainingSeconds,

        @Schema(description = "상위 랭커 목록 (최대 10명, 순위 오름차순)") List<LeagueRankerResponse> rankers,

        @Schema(description = "내 순위 및 스낵 현황") LeagueMyRankResponse me
) {
}
