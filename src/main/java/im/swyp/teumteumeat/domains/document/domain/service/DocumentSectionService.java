package im.swyp.teumteumeat.domains.document.domain.service;

import im.swyp.teumteumeat.domains.document.domain.constant.DocumentResponseCode;
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

/**
 * PDF 원문을 목표 진행 일자(N일)에 맞춰 지연(lazy) 분할/조회한다.
 * 최초 조회 시 {@link DocumentSectionSeeder}로 결정적 분할해 저장하고, 이후에는 저장된 섹션을 재사용한다.
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
    private final DocumentSectionSeeder documentSectionSeeder;

    @Transactional
    public String resolveCurrentSectionContent(Long documentId, Goal goal) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BaseException(DocumentResponseCode.NOT_FOUND_DOCUMENT));

        int totalSections = Math.max(DEFAULT_TOTAL_SECTIONS, goal.getTargetQuizSetCount());
        int currentIndex = goal.getCompletedQuizSetCount();

        List<DocumentSection> sections = documentSectionRepository
                .findByDocumentIdAndTotalSectionsOrderBySectionIndex(documentId, totalSections);

        if (sections.isEmpty()) {
            // seed는 별도 트랜잭션(REQUIRES_NEW)에서 수행해, unique 제약 위반으로 flush가
            // 실패해도 현재 트랜잭션의 영속성 컨텍스트가 오염되지 않도록 격리한다.
            try {
                sections = documentSectionSeeder.seedSections(document, totalSections);
            } catch (DataIntegrityViolationException e) {
                // 동시 요청으로 이미 다른 트랜잭션이 생성한 경우, 현재(오염되지 않은) 트랜잭션에서 재조회
                sections = documentSectionRepository
                        .findByDocumentIdAndTotalSectionsOrderBySectionIndex(documentId, totalSections);
            }
        }

        // 문서가 짧아 실제 섹션 수가 목표 일수보다 적으면, 처음부터 순환하며 재사용
        int cycleIndex = Math.max(currentIndex, 0) % sections.size();
        return sections.get(cycleIndex).getContent();
    }
}
