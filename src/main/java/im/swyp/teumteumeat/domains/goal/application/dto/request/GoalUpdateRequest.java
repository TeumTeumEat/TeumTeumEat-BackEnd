package im.swyp.teumteumeat.domains.goal.application.dto.request;

import java.time.LocalDate;

public record GoalUpdateRequest(

        LocalDate endDate
) {
}
