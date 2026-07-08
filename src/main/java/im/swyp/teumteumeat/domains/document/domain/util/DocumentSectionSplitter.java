package im.swyp.teumteumeat.domains.document.domain.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PDF 원문을 목표 기간(N일)만큼의 구간으로 결정적(deterministic)으로 분할한다.
 * LLM/임베딩 호출 없이 문단 -> 문장 -> 단어 -> 강제절단 순으로 경계를 찾아
 * 그리디 빈패킹으로 채우며, 반환된 구간을 순서대로 이어붙이면 원문과 완전히 동일하다.
 */
public final class DocumentSectionSplitter {

    public static final int DEFAULT_MIN_SECTION_CHARS = 800;

    private static final Pattern PARAGRAPH_BOUNDARY = Pattern.compile("\\n\\s*\\n+");
    private static final Pattern SENTENCE_BOUNDARY = Pattern.compile("[.!?]+\\s+");
    private static final Pattern WORD_BOUNDARY = Pattern.compile("\\s+");

    private DocumentSectionSplitter() {
    }

    public static List<String> split(String rawText, int requestedSections) {
        return split(rawText, requestedSections, DEFAULT_MIN_SECTION_CHARS);
    }

    public static List<String> split(String rawText, int requestedSections, int minSectionChars) {
        if (rawText == null || rawText.isEmpty()) {
            throw new IllegalArgumentException("rawText는 비어 있을 수 없습니다.");
        }
        if (requestedSections < 1) {
            throw new IllegalArgumentException("requestedSections는 1 이상이어야 합니다.");
        }
        if (minSectionChars < 1) {
            throw new IllegalArgumentException("minSectionChars는 1 이상이어야 합니다.");
        }

        int effectiveSections = Math.min(requestedSections, Math.max(1, rawText.length() / minSectionChars));

        List<String> atomicUnits = tokenize(rawText);

        return packIntoChunks(atomicUnits, effectiveSections);
    }

    /**
     * 문단 -> 문장 -> 단어 -> 강제절단 순으로, 실제로 경계가 존재하는 첫 단계를 채택한다.
     * 각 경계 문자(구분자)는 다음 조각이 아니라 앞 조각에 포함시켜, 조각들을 순서대로
     * 이어붙이면 원문과 완전히 동일하도록 보장한다.
     */
    private static List<String> tokenize(String text) {
        List<String> byParagraph = tokenizeByBoundary(text, PARAGRAPH_BOUNDARY);
        if (byParagraph.size() > 1) {
            return byParagraph;
        }

        List<String> bySentence = tokenizeByBoundary(text, SENTENCE_BOUNDARY);
        if (bySentence.size() > 1) {
            return bySentence;
        }

        List<String> byWord = tokenizeByBoundary(text, WORD_BOUNDARY);
        if (byWord.size() > 1) {
            return byWord;
        }

        return tokenizeByCharacter(text);
    }

    private static List<String> tokenizeByBoundary(String text, Pattern boundaryPattern) {
        List<String> units = new ArrayList<>();
        Matcher matcher = boundaryPattern.matcher(text);

        int start = 0;
        while (matcher.find()) {
            units.add(text.substring(start, matcher.end()));
            start = matcher.end();
        }
        if (start < text.length()) {
            units.add(text.substring(start));
        }
        return units;
    }

    private static List<String> tokenizeByCharacter(String text) {
        List<String> units = new ArrayList<>(text.length());
        for (int i = 0; i < text.length(); i++) {
            units.add(String.valueOf(text.charAt(i)));
        }
        return units;
    }

    /**
     * 목표 구간 크기(전체 길이 / sections)를 넘기기 전까지 단위를 누적하는 그리디 빈패킹.
     * 마지막 구간은 남은 단위를 모두 흡수해, 반환 구간 수는 항상 sections 이하가 되도록 한다.
     */
    private static List<String> packIntoChunks(List<String> atomicUnits, int sections) {
        int totalLength = atomicUnits.stream().mapToInt(String::length).sum();
        int targetSize = Math.max(1, totalLength / sections);

        List<String> result = new ArrayList<>();
        int unitIndex = 0;
        int unitCount = atomicUnits.size();

        for (int sectionIndex = 0; sectionIndex < sections - 1 && unitIndex < unitCount; sectionIndex++) {
            StringBuilder current = new StringBuilder();
            while (unitIndex < unitCount) {
                String unit = atomicUnits.get(unitIndex);
                if (!current.isEmpty() && current.length() + unit.length() > targetSize) {
                    break;
                }
                current.append(unit);
                unitIndex++;
            }
            result.add(current.toString());
        }

        if (unitIndex < unitCount) {
            StringBuilder last = new StringBuilder();
            while (unitIndex < unitCount) {
                last.append(atomicUnits.get(unitIndex));
                unitIndex++;
            }
            result.add(last.toString());
        }

        return result;
    }
}
