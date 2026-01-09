-- 1) 새 ENUM 컬럼 추가
ALTER TABLE `CareCallSetting`
    ADD COLUMN `recurrence_new` ENUM('DAILY','WEEKLY','MONTHLY') NULL;

-- 2) 기존 TINYINT 값을 ENUM 문자열로 백필
UPDATE `CareCallSetting`
SET `recurrence_new` = CASE `recurrence`
                          WHEN 0 THEN 'DAILY'
                          WHEN 1 THEN 'WEEKLY'
                          WHEN 2 THEN 'MONTHLY'
                          ELSE NULL
    END;

-- 3) 변환 실패 건수 확인 (0인지 체크)
SELECT COUNT(*) AS invalid_rows
FROM `CareCallSetting`
WHERE `recurrence_new` IS NULL;

-- invalid_rows = 0 확인 후 4) 기존 컬럼 제거 + 새 컬럼을 recurrence로 전환
ALTER TABLE `CareCallSetting`
DROP COLUMN `recurrence`,
    CHANGE COLUMN `recurrence_new` `recurrence` ENUM('DAILY','WEEKLY','MONTHLY') NOT NULL;
