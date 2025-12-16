package im.swyp.teumteumeat.domains.userQuiz.domain.service;

import im.swyp.teumteumeat.domains.userQuiz.persistence.entity.UserQuiz;
import im.swyp.teumteumeat.domains.userQuiz.persistence.repository.UserQuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQuizService {

    private final UserQuizRepository userQuizRepository;

    @Transactional
    public void saveUserQuiz(UserQuiz userQuiz) {
        userQuizRepository.save(userQuiz);
    }
}
