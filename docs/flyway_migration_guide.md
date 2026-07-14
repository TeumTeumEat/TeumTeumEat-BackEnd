# Flyway 마이그레이션 작성 가이드

DB 스키마 변경(테이블 생성, 컬럼 추가/변경, 삭제 등)이 있는 PR은 반드시 Flyway 마이그레이션 SQL을 함께 작성합니다.

## 기본 규칙

- 위치: `src/main/resources/db/migration/`
- 파일명: `V{n}__설명.sql` (예: `V4__add_notification_read_at.sql`)
- 마이그레이션 하나 = 논리적으로 하나의 스키마 변경. 성격이 다른 변경(예: 테이블 생성 / 컬럼 타입 변경)은 파일을 분리합니다.
- 이미 병합되어 배포된 버전 파일은 수정하지 않습니다. 잘못됐다면 새 버전 파일로 수정합니다.

## 작성 전 반드시 확인할 것

1. local/develop은 `ddl-auto: update`라서 엔티티 변경 시 자동으로 스키마가 바뀝니다. prod는 `ddl-auto: validate`라서 자동 반영되지 않습니다. "로컬 DB에 이미 있는 컬럼/테이블"이라도 운영에 반영하려면 마이그레이션이 필요합니다.
2. **새 마이그레이션은 반드시 멱등(idempotent)하게 작성할 것.**
   - `CREATE TABLE IF NOT EXISTS ...`
   - `ALTER TABLE ... MODIFY COLUMN ...`
   - `ALTER TABLE ... ADD COLUMN IF NOT EXISTS ...`
3. **실제 스키마를 알 수 있으면 엔티티 추론보다 우선 참고할 것.** `mysqldump -h<host> -P<port> -u<user> -p'<password>' --no-data <db>`로 실제 DDL을 뽑아 대조합니다.
4. **커밋 전 실제로 적용해볼 것.** 로컬 새 마이그레이션 파일을 순서대로 적용해보고, 에러 없이 끝까지 실행되는지 확인합니다.

## 예시

```sql
-- V4__add_notification_read_at.sql
-- Notification.readAt 필드 추가, 운영 ddl-auto:validate라 자동 반영 안 됨

ALTER TABLE `notification`
    ADD COLUMN IF NOT EXISTS `read_at` datetime(6) DEFAULT NULL;
```

참고 파일 : `src/main/resources/db/migration/V2__create_document_section.sql`, `V3__alter_document_raw_content_mediumtext.sql`
