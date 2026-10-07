-- 리그 스낵 적립 기록(SnackHistory) 테이블 생성, 운영 ddl-auto:validate라 자동 반영 안 됨

CREATE TABLE IF NOT EXISTS `snack_history`
(
    `snack_history_id` bigint      NOT NULL AUTO_INCREMENT,
    `created_date`     datetime(6) DEFAULT NULL,
    `updated_at`       datetime(6) DEFAULT NULL,
    `user_id`          bigint      NOT NULL,
    PRIMARY KEY (`snack_history_id`),
    KEY `idx_snack_history_created_date_user` (`created_date`, `user_id`),
    KEY `idx_snack_history_user_created_date` (`user_id`, `created_date`),
    CONSTRAINT `fk_snack_history_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
