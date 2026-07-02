package im.swyp.teumteumeat.domains.document.domain.service;

import im.swyp.teumteumeat.domains.document.domain.constant.DocumentResponseCode;
import im.swyp.teumteumeat.domains.document.domain.util.DocumentSectionSplitter;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentSection;
import im.swyp.teumteumeat.domains.document.persistence.repository.DocumentRepository;
import im.swyp.teumteumeat.domains.document.persistence.repository.DocumentSectionRepository;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

/**
 * PDF 원문을 목표 진행 일자(N일)에 맞춰 지연(lazy) 분할/조회한다.
 * 최초 조회 시 {@link DocumentSectionSplitter}로 결정적 분할해 저장하고, 이후에는 저장된 섹션을 재사용한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentSectionService {

    // 앱에서 목표 기간을 7/14/21/28일 중 하나로만 설정하므로, targetQuizSetCount가
    // 비정상적으로 0(엔티티 기본값)이어도 최소 목표 기간(7일)만큼은 분할되도록 보장
    private static final int DEFAULT_TOTAL_SECTIONS = 7;

    private final DocumentRepository documentRepository;
    private final DocumentSectionRepository documentSectionRepository;

    @Transactional
    public String resolveCurrentSectionContent(Long documentId, Goal goal) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BaseException(DocumentResponseCode.NOT_FOUND_DOCUMENT));

        int totalSections = Math.max(DEFAULT_TOTAL_SECTIONS, goal.getTargetQuizSetCount());
        int currentIndex = goal.getCompletedQuizSetCount();

        List<DocumentSection> sections = documentSectionRepository
                .findByDocumentIdAndTotalSectionsOrderBySectionIndex(documentId, totalSections);

        if (sections.isEmpty()) {
            sections = seedSections(document, totalSections);
        }

        // 문서가 짧아 실제 섹션 수가 목표 일수보다 적으면, 마지막 섹션을 재사용
        int clampedIndex = Math.min(Math.max(currentIndex, 0), sections.size() - 1);
        return sections.get(clampedIndex).getContent();
    }

    private List<DocumentSection> seedSections(Document document, int totalSections) {
        List<String> contents = DocumentSectionSplitter.split(document.getRawContent(), totalSections);

        List<DocumentSection> sections = IntStream.range(0, contents.size())
                .mapToObj(index -> DocumentSection.builder()
                        .document(document)
                        .totalSections(totalSections)
                        .sectionIndex(index)
                        .content(contents.get(index))
                        .build())
                .toList();

        // 동시 요청시 재생성이 아닌 기존 생성된 로직 반환
        try {
            return documentSectionRepository.saveAllAndFlush(sections);
        } catch (DataIntegrityViolationException e) {
            return documentSectionRepository
                    .findByDocumentIdAndTotalSectionsOrderBySectionIndex(document.getId(), totalSections);
        }
    }
}
