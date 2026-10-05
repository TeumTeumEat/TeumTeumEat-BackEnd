package im.swyp.teumteumeat.domains.league.domain.util;

/**
 * 리그 닉네임 노출 정책 (LGS-004)
 * 첫 글자와 마지막 글자만 남기고 중간 글자를 모두 '*'로 마스킹한다.
 * ex) 김지민 → 김*민, 김네글자 → 김**자, TestUser → T******r, 김지 → 김*
 */
public final class NameMasker {

    private static final String MASK = "*";
    private static final String UNKNOWN_NAME = "익명";

    private NameMasker() {
    }

    public static String mask(String name) {
        if (name == null || name.isBlank()) {
            return UNKNOWN_NAME;
        }

        int[] codePoints = name.strip().codePoints().toArray();
        int length = codePoints.length;

        if (length == 1) {
            return new String(codePoints, 0, 1);
        }
        if (length == 2) {
            return new String(codePoints, 0, 1) + MASK;
        }
        return new String(codePoints, 0, 1)
                + MASK.repeat(length - 2)
                + new String(codePoints, length - 1, 1);
    }
}
