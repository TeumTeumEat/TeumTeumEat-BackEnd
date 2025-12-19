package im.swyp.teumteumeat.domains.document.domain.service;

import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import org.springframework.stereotype.Service;

@Service
public class OCRService {

    public void extractContent(Document document) {
        // Mock OCR
        String mockContent = "테스트를 위한 가상 문서" +
                "추후 Presigned URL을 통해 업로드된 PDF 파일의 내용을 분석하여 퀴즈 생성";
        document.updateRawContent(mockContent);
    }
}
