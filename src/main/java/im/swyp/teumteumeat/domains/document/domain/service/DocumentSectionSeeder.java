package im.swyp.teumteumeat.domains.document.domain.service;

import im.swyp.teumteumeat.domains.document.domain.util.DocumentSectionSplitter;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentSection;
import im.swyp.teumteumeat.domains.document.persistence.repository.DocumentSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

/**
 * 섹션 최초 생성(INSERT)만 별도 트랜잭션으로 격리한다.
 * unique 제약 위반으로 flush가 실패해도, 호출자({@link DocumentSectionService})가 쓰던
 * 영속성 컨텍스트가 오염된 상태로 재사용되지 않도록 REQUIRES_NEW로 분리했다.
 */
@Component
@RequiredArgsConstructor
public class DocumentSectionSeeder {

    private final DocumentSectionRepository documentSectionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<DocumentSection> seedSections(Document document, int totalSections) {
        List<String> contents = DocumentSectionSplitter.split(document.getRawContent(), totalSections);

        List<DocumentSection> sections = IntStream.range(0, contents.size())
                .mapToObj(index -> DocumentSection.builder()
                        .document(document)
                        .totalSections(totalSections)
                        .sectionIndex(index)
                        .content(contents.get(index))
                        .build())
                .toList();

        return documentSectionRepository.saveAllAndFlush(sections);
    }
}
