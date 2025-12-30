package im.swyp.teumteumeat.infra.ocr.application.dto;

import java.util.List;

public record OcrResponse(List<Image> images) {
    public record Image(List<Field> fields) {}
    public record Field(String inferText, boolean lineBreak) {}

    // 모든 텍스트를 하나로 합치는 편의 메서드
    public String getFullText() {
        if (images == null || images.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (Field field : images.get(0).fields()) {
            sb.append(field.inferText());
            if (field.lineBreak()) {
                sb.append("\n"); // 줄바꿈 적용
            } else {
                sb.append(" ");  // 단어 사이 공백
            }
        }
        return sb.toString().trim();
    }
}