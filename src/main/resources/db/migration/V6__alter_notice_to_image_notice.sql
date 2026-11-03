-- 공지(Notice)를 텍스트 공지에서 이미지 공지로 변경, 운영 ddl-auto:validate라 자동 반영 안 됨

-- 기존 텍스트 공지는 이미지가 없어 모달/전체화면으로 띄울 수 없으므로 삭제 처리
UPDATE `notice`
SET `is_deleted` = 1;

-- 기존 행의 이미지 key는 NOT NULL 암묵적 기본값('')으로 채워짐 (위에서 삭제 처리됨)
ALTER TABLE `notice`
    DROP COLUMN `content`,
    ADD COLUMN `full_screen_image_key` varchar(255) NOT NULL,
    ADD COLUMN `modal_image_key`       varchar(255) NOT NULL,
    ADD COLUMN `external_link`         varchar(500) DEFAULT NULL,
    ADD COLUMN `deep_link`             varchar(500) DEFAULT NULL,
    ADD COLUMN `action_button_text`    varchar(20)  DEFAULT NULL,
    ADD COLUMN `popup_start_at`        datetime(6)  DEFAULT NULL,
    ADD COLUMN `popup_end_at`          datetime(6)  DEFAULT NULL;

-- datetime NOT NULL은 기존 행에 0000-00-00이 들어가 strict 모드에서 실패하므로, 기존 행을 채운 뒤 NOT NULL로 변경 (위에서 삭제 처리됨)
UPDATE `notice`
SET `popup_start_at` = COALESCE(`created_date`, NOW(6)),
    `popup_end_at`   = COALESCE(`created_date`, NOW(6));

ALTER TABLE `notice`
    MODIFY COLUMN `popup_start_at` datetime(6) NOT NULL,
    MODIFY COLUMN `popup_end_at`   datetime(6) NOT NULL;
