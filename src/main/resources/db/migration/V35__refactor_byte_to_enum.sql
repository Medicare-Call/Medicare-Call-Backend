-- 1. Elder 테이블 gender 마이그레이션
ALTER TABLE `Elder` ADD COLUMN `gender_new` ENUM('MALE','FEMALE') NULL;

UPDATE `Elder`
SET `gender_new` = CASE `gender`
                       WHEN 0 THEN 'MALE'
                       WHEN 1 THEN 'FEMALE'
                       ELSE NULL
    END;

ALTER TABLE `Elder` DROP COLUMN `gender`;
ALTER TABLE `Elder` CHANGE COLUMN `gender_new` `gender` ENUM('MALE','FEMALE') NOT NULL;

-- 2. Member 테이블 plan 마이그레이션
-- 기존 Byte 값 매핑: 1 -> STANDARD, 2 -> PREMIUM (기획에 맞게 수정 필요)
ALTER TABLE `Member` ADD COLUMN `plan_new` ENUM('STANDARD','PREMIUM') NULL;

UPDATE `Member`
SET `plan_new` = CASE `plan`
                     WHEN 1 THEN 'STANDARD'
                     WHEN 2 THEN 'PREMIUM'
                     ELSE NULL
    END;

ALTER TABLE `Member` DROP COLUMN `plan`;
ALTER TABLE `Member` CHANGE COLUMN `plan_new` `plan` ENUM('STANDARD','PREMIUM') NULL;