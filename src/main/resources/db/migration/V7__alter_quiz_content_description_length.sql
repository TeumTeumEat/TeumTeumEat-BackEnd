-- 퀴즈 질문/해설 글자 수 제한은 프롬프트로만 유도하고, DB 컬럼은 LLM 출력이 조금 넘쳐도 저장되도록 넉넉하게 확장
-- (기존 varchar(80)/varchar(120)에서 질문 하나만 넘쳐도 saveAll 세트 전체가 롤백되던 문제, Sentry TEUMTEUMEAT-BACKEND-A)
ALTER TABLE `quiz`
    MODIFY COLUMN `content`     varchar(255) NOT NULL,
    MODIFY COLUMN `description` varchar(255) NOT NULL;
