package im.swyp.teumteumeat.domains.league.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record LeagueRankerResponse(

        @Schema(description = "순위", example = "1") int rank,

        @Schema(description = "마스킹된 닉네임", example = "김*민") String name,

        @Schema(description = "이번 주 스낵 수", example = "42") long weeklySnackCount,

        @Schema(description = "본인 여부", example = "false") boolean isMe
) {
}
