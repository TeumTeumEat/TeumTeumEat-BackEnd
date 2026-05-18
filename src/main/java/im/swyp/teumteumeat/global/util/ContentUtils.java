package im.swyp.teumteumeat.global.util;

public class ContentUtils {

    private static final int DEFAULT_TRUNCATE_LENGTH = 800;

    /**
     * 내용을 안전하게 자릅니다.
     * 지정된 길이로 자르되, 마지막 문장이 완성되도록 마침표(.)를 기준으로 자릅니다.
     *
     * @param content 원본 내용
     * @return 잘린 내용
     */
    public static String truncateContentSafe(String content) {
        if (content == null || content.length() <= DEFAULT_TRUNCATE_LENGTH) {
            return content;
        }

        String truncated = content.substring(0, DEFAULT_TRUNCATE_LENGTH);
        int lastPeriodIndex = truncated.lastIndexOf(".");
        if (lastPeriodIndex != -1) {
            return truncated.substring(0, lastPeriodIndex + 1);
        }
        return truncated;
    }
}
