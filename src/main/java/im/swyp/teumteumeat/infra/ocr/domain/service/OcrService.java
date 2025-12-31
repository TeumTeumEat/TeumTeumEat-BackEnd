package im.swyp.teumteumeat.infra.ocr.domain.service;

import im.swyp.teumteumeat.infra.ocr.application.dto.OcrResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OcrService {

    @Value("${infra.naver.invoke-url}")
    private String url;

    @Value("${infra.naver.secret-key}")
    private String secretKey;

    private final RestClient restClient;

    /**
     * 이미지(PDF) 파일에서 OCR로 텍스트를 추출하여 반환하는 메서드
     *
     * <p>API 명세서 : <a href="https://api.ncloud-docs.com/docs/ai-application-service-ocr-ocr">CLOVA OCR</a>
     * @param imageUrl s3 파일 경로
     * @return 추출한 텍스트
     */
    public String extractText(String imageUrl) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("version", "V2");
        requestBody.put("requestId", UUID.randomUUID());
        requestBody.put("timestamp", System.currentTimeMillis());

        List<Map<String, Object>> imageList = new ArrayList<>();
        Map<String, Object> images = new HashMap<>();
        images.put("format", "pdf");
        images.put("name", UUID.randomUUID());
        images.put("url", imageUrl);
        imageList.add(images);
        requestBody.put("images", imageList);

        OcrResponse response = restClient.post()
                .uri(url)
                .header("X-OCR-SECRET", secretKey)
                .body(requestBody)
                .retrieve()
                .body(OcrResponse.class);

        return (response != null) ? response.getFullText() : "";
    }
}
