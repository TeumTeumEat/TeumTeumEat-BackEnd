package im.swyp.teumteumeat.domains.goal.domain.util;

import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;

import java.time.LocalDate;

public class DateParser {

    public static LocalDate calculateEndDate(LocalDate startDate, String studyPeriod) {
        LocalDate endDate;
        try {
            int weeks = Integer.parseInt(studyPeriod.replace("주", ""));
            endDate = startDate.plusWeeks(weeks);
        } catch (NumberFormatException e) {
            throw new BaseException(CommonResponseCode.BAD_REQUEST);
        }
        return endDate;
    }
}
