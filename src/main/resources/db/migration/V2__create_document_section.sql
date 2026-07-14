CREATE TABLE IF NOT EXISTS `document_section`
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
