-- Baseline schema, captured via mysqldump --no-data from the actual dev DB
-- (Flyway baseline-version = 1). Preserves real Hibernate-generated constraint
-- names and columns (including legacy `document.summary`) rather than a
-- hand-reconstructed schema, so this matches production exactly.

CREATE TABLE `category`
(
    `category_id`  bigint       NOT NULL AUTO_INCREMENT,
    `created_date` datetime(6)  DEFAULT NULL,
    `updated_at`   datetime(6)  DEFAULT NULL,
    `description`  varchar(255) DEFAULT NULL,
    `name`         varchar(255) DEFAULT NULL,
    `path`         varchar(255) DEFAULT NULL,
    PRIMARY KEY (`category_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `category_subtopic`
(
    `subtopic_id`    bigint      NOT NULL AUTO_INCREMENT,
    `created_date`   datetime(6) DEFAULT NULL,
    `updated_at`     datetime(6) DEFAULT NULL,
    `duration_weeks` int         NOT NULL,
    `sequence_index` int         NOT NULL,
    `title`          varchar(60) NOT NULL,
    `category_id`    bigint      NOT NULL,
    PRIMARY KEY (`subtopic_id`),
    UNIQUE KEY `UKc8qx7497s35jx3icvb7u371c9` (`category_id`, `duration_weeks`, `sequence_index`),
    CONSTRAINT `FKl21axpn6k0o3ohujnhbuofvae` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `commute_info`
(
    `end_time`        time(6)     DEFAULT NULL,
    `start_time`      time(6)     DEFAULT NULL,
    `usage_time`      int         NOT NULL,
    `commute_info_id` bigint      NOT NULL AUTO_INCREMENT,
    `created_date`    datetime(6) DEFAULT NULL,
    `updated_at`      datetime(6) DEFAULT NULL,
    PRIMARY KEY (`commute_info_id`),
    KEY `idx_commute_start` (`start_time`),
    KEY `idx_commute_end` (`end_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `users`
(
    `onboarding_completed`       bit(1)      NOT NULL,
    `commute_info_id`            bigint      DEFAULT NULL,
    `created_date`               datetime(6) DEFAULT NULL,
    `updated_at`                 datetime(6) DEFAULT NULL,
    `user_id`                    bigint      NOT NULL AUTO_INCREMENT,
    `email`                      varchar(255) DEFAULT NULL,
    `name`                       varchar(255) DEFAULT NULL,
    `social_id`                  varchar(255) DEFAULT NULL,
    `role`                       enum ('ADMIN','USER') DEFAULT NULL,
    `social_provider`            enum ('APPLE','GOOGLE','KAKAO') DEFAULT NULL,
    `push_enabled`               bit(1)      NOT NULL,
    `social_refresh_token`       varchar(255) DEFAULT NULL,
    `current_goal_id`            bigint      DEFAULT NULL,
    `quiz_guide_seen`            bit(1)      NOT NULL,
    `available_quiz_count`       int         DEFAULT '1',
    `last_quiz_count_reset_date` date        DEFAULT NULL,
    `daily_ad_reward_count`      int         DEFAULT '0',
    `status`                     enum ('PENDING','ACTIVE') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `UK8axbbqrcmff5cyfsprms933ph` (`commute_info_id`),
    UNIQUE KEY `UK45t1ax15b065viosrfvibs499` (`current_goal_id`),
    UNIQUE KEY `uk_social` (`social_provider`, `social_id`),
    CONSTRAINT `FK2w09sg41nhq71kfoqmn6tgum7` FOREIGN KEY (`commute_info_id`) REFERENCES `commute_info` (`commute_info_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `goal`
(
    `end_date`                 date        DEFAULT NULL,
    `category_id`              bigint      DEFAULT NULL,
    `created_date`             datetime(6) DEFAULT NULL,
    `goal_id`                  bigint      NOT NULL AUTO_INCREMENT,
    `updated_at`               datetime(6) DEFAULT NULL,
    `user_id`                  bigint      NOT NULL,
    `type`                     enum ('CATEGORY','DOCUMENT') DEFAULT NULL,
    `difficulty`               enum ('EASY','HARD','MEDIUM') DEFAULT NULL,
    `prompt`                   varchar(30) DEFAULT NULL,
    `completed_quiz_set_count` int         DEFAULT '0',
    `is_completed`             tinyint(1)  DEFAULT '0',
    `target_quiz_set_count`    int         DEFAULT '0',
    PRIMARY KEY (`goal_id`),
    KEY `FKiadq59lfc3x9h9jvxouon7niq` (`category_id`),
    KEY `FKf70arauooy8e5a5egk8k69xdr` (`user_id`),
    CONSTRAINT `FKf70arauooy8e5a5egk8k69xdr` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
    CONSTRAINT `FKiadq59lfc3x9h9jvxouon7niq` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

ALTER TABLE `users`
    ADD CONSTRAINT `FKg05113kicashmevfeo22jqld6` FOREIGN KEY (`current_goal_id`) REFERENCES `goal` (`goal_id`);

CREATE TABLE `document`
(
    `document_id`   bigint      NOT NULL AUTO_INCREMENT,
    `created_date`  datetime(6) DEFAULT NULL,
    `updated_at`    datetime(6) DEFAULT NULL,
    `file_key`      varchar(255) DEFAULT NULL,
    `file_name`     varchar(255) DEFAULT NULL,
    `raw_content`   mediumtext,
    `status`        enum ('COMPLETED','FAILED','PENDING','PROCESSING') DEFAULT NULL,
    `summary`       varchar(500) DEFAULT NULL,
    `goal_id`       bigint      DEFAULT NULL,
    `user_id`       bigint      DEFAULT NULL,
    `title`         varchar(255) DEFAULT NULL,
    `total_parts`   int         DEFAULT NULL,
    `estimate_time` datetime    DEFAULT NULL,
    `error_reason`  enum ('ENCRYPTED_FILE','SERVER_ERROR','TIMEOUT') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    PRIMARY KEY (`document_id`),
    KEY `FKkqonmf1mf9vyj77gd8byecfpa` (`goal_id`),
    KEY `FKm19xjdnh3l6aueyrpm1705t52` (`user_id`),
    CONSTRAINT `FKkqonmf1mf9vyj77gd8byecfpa` FOREIGN KEY (`goal_id`) REFERENCES `goal` (`goal_id`),
    CONSTRAINT `FKm19xjdnh3l6aueyrpm1705t52` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `document_part`
(
    `document_part_id` bigint      NOT NULL AUTO_INCREMENT,
    `created_date`      datetime(6) DEFAULT NULL,
    `updated_at`        datetime(6) DEFAULT NULL,
    `ocr_text`          longtext,
    `part_index`        int         DEFAULT NULL,
    `document_id`       bigint      NOT NULL,
    PRIMARY KEY (`document_part_id`),
    KEY `FKslya3o7doudxbgk94v5ostf28` (`document_id`),
    CONSTRAINT `FKslya3o7doudxbgk94v5ostf28` FOREIGN KEY (`document_id`) REFERENCES `document` (`document_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `document_section`
(
    `document_section_id` bigint      NOT NULL AUTO_INCREMENT,
    `created_date`         datetime(6) DEFAULT NULL,
    `updated_at`           datetime(6) DEFAULT NULL,
    `content`              mediumtext NOT NULL,
    `section_index`        int        NOT NULL,
    `total_sections`       int        NOT NULL,
    `document_id`          bigint     NOT NULL,
    PRIMARY KEY (`document_section_id`),
    UNIQUE KEY `UKdh6xn8y5p6hxt2960yx8j1e2s` (`document_id`, `total_sections`, `section_index`),
    CONSTRAINT `FKk1sv40tl5vkfxjk1eajs70nj8` FOREIGN KEY (`document_id`) REFERENCES `document` (`document_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `document_summary`
(
    `document_summary_id` bigint      NOT NULL AUTO_INCREMENT,
    `created_date`         datetime(6) DEFAULT NULL,
    `updated_at`           datetime(6) DEFAULT NULL,
    `summary`              varchar(2000) DEFAULT NULL,
    `title`                varchar(255)  DEFAULT NULL,
    `document_id`          bigint      NOT NULL,
    PRIMARY KEY (`document_summary_id`),
    UNIQUE KEY `uk_document_summary_doc_date` (`document_id`, `created_date`),
    CONSTRAINT `FKmq9suq1bncooc4a5sgmhl92p2` FOREIGN KEY (`document_id`) REFERENCES `document` (`document_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `category_document`
(
    `category_document_id` bigint      NOT NULL AUTO_INCREMENT,
    `created_date`          datetime(6) DEFAULT NULL,
    `updated_at`            datetime(6) DEFAULT NULL,
    `content`               varchar(2000) NOT NULL,
    `category_id`           bigint      NOT NULL,
    `title`                 varchar(255) DEFAULT NULL,
    `goal_id`               bigint      DEFAULT NULL,
    `category_subtopic_id`  bigint      DEFAULT NULL,
    PRIMARY KEY (`category_document_id`),
    UNIQUE KEY `uk_category_document_goal_date` (`goal_id`, `created_date`),
    KEY `FKtit6io0uev13epb8wb8emk2cr` (`category_id`),
    KEY `FKdona01lxxgjj5rlct9psq6iul` (`category_subtopic_id`),
    CONSTRAINT `FKdona01lxxgjj5rlct9psq6iul` FOREIGN KEY (`category_subtopic_id`) REFERENCES `category_subtopic` (`subtopic_id`),
    CONSTRAINT `FKfr84q3ywhnhdalmo3cf5ew38c` FOREIGN KEY (`goal_id`) REFERENCES `goal` (`goal_id`),
    CONSTRAINT `FKtit6io0uev13epb8wb8emk2cr` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `quiz`
(
    `quiz_id`              bigint      NOT NULL AUTO_INCREMENT,
    `created_date`         datetime(6) DEFAULT NULL,
    `updated_at`           datetime(6) DEFAULT NULL,
    `answer`               varchar(255) NOT NULL,
    `content`              varchar(80)  NOT NULL,
    `description`          varchar(120) NOT NULL,
    `options`              text,
    `quiz_type`            enum ('MCQ','OX') DEFAULT NULL,
    `category_document_id` bigint      DEFAULT NULL,
    `document_id`          bigint      DEFAULT NULL,
    `difficulty`           enum ('EASY','HARD','MEDIUM') NOT NULL,
    `topic`                varchar(30)  DEFAULT NULL,
    `document_summary_id`  bigint      DEFAULT NULL,
    PRIMARY KEY (`quiz_id`),
    KEY `FKjqeyibfq2flrgcu2w1a17mc3v` (`category_document_id`),
    KEY `FKb41ie22wx0phv65wdfpcrm1ll` (`document_id`),
    KEY `FK70t02tu8ceykye5n8gw4t5sdf` (`document_summary_id`),
    CONSTRAINT `FK70t02tu8ceykye5n8gw4t5sdf` FOREIGN KEY (`document_summary_id`) REFERENCES `document_summary` (`document_summary_id`),
    CONSTRAINT `FKb41ie22wx0phv65wdfpcrm1ll` FOREIGN KEY (`document_id`) REFERENCES `document` (`document_id`),
    CONSTRAINT `FKjqeyibfq2flrgcu2w1a17mc3v` FOREIGN KEY (`category_document_id`) REFERENCES `category_document` (`category_document_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `user_quiz`
(
    `id`           bigint      NOT NULL AUTO_INCREMENT,
    `created_date` datetime(6) DEFAULT NULL,
    `updated_at`   datetime(6) DEFAULT NULL,
    `is_correct`   bit(1)      NOT NULL,
    `quiz_id`      bigint      NOT NULL,
    `user_id`      bigint      NOT NULL,
    `goal_id`      bigint      DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FKamnut7xcu11shpwa926626y87` (`quiz_id`),
    KEY `FKeuoc6oyjhqpnqf4iufs0j6t8b` (`user_id`),
    KEY `FKavynfvsivj68qm6ik943ddyqi` (`goal_id`),
    CONSTRAINT `FKamnut7xcu11shpwa926626y87` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`quiz_id`),
    CONSTRAINT `FKavynfvsivj68qm6ik943ddyqi` FOREIGN KEY (`goal_id`) REFERENCES `goal` (`goal_id`),
    CONSTRAINT `FKeuoc6oyjhqpnqf4iufs0j6t8b` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `device_token`
(
    `device_token_id` bigint      NOT NULL AUTO_INCREMENT,
    `created_date`     datetime(6) DEFAULT NULL,
    `updated_at`       datetime(6) DEFAULT NULL,
    `device_type`      enum ('ANDROID','IOS') NOT NULL,
    `token`            varchar(255) NOT NULL,
    `user_id`          bigint      NOT NULL,
    PRIMARY KEY (`device_token_id`),
    UNIQUE KEY `UKoaccue9kxei35rbe5thnv18ye` (`token`),
    KEY `FKdklq4fbedbwx14v2varmsjeb5` (`user_id`),
    CONSTRAINT `FKdklq4fbedbwx14v2varmsjeb5` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
