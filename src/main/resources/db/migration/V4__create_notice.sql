-- 공지(Notice) 테이블 생성, 운영 ddl-auto:validate라 자동 반영 안 됨

CREATE TABLE IF NOT EXISTS `notice`
(
    `notice_id`    bigint        NOT NULL AUTO_INCREMENT,
    `created_date` datetime(6)   DEFAULT NULL,
    `updated_at`   datetime(6)   DEFAULT NULL,
    `title`        varchar(100)  DEFAULT NULL,
    `content`      varchar(5000) DEFAULT NULL,
    `is_deleted`   tinyint(1)    DEFAULT '0',
    PRIMARY KEY (`notice_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
