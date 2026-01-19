-- CareCallRecord.responded를 ENUM 문자열로 전환
ALTER TABLE `CareCallRecord`
    ADD COLUMN `responded_new` ENUM('RESPONDED','NOT_RESPONDED') NOT NULL DEFAULT 'NOT_RESPONDED';

UPDATE `CareCallRecord`
SET `responded_new` = CASE `responded`
                         WHEN 1 THEN 'RESPONDED'
                         WHEN 0 THEN 'NOT_RESPONDED'
                         ELSE 'NOT_RESPONDED'
    END;

SELECT COUNT(*) AS invalid_responded_rows
FROM `CareCallRecord`
WHERE `responded` NOT IN (0, 1);

ALTER TABLE `CareCallRecord`
    DROP COLUMN `responded`,
    CHANGE COLUMN `responded_new` `responded` ENUM('RESPONDED','NOT_RESPONDED') NOT NULL;

-- CareCallRecord.health_status를 ENUM 문자열로 전환
ALTER TABLE `CareCallRecord`
    ADD COLUMN `health_status_new` ENUM('GOOD','BAD') NULL;

UPDATE `CareCallRecord`
SET `health_status_new` = CASE `health_status`
                             WHEN 1 THEN 'GOOD'
                             WHEN 0 THEN 'BAD'
                             ELSE NULL
    END;

SELECT COUNT(*) AS invalid_health_status_rows
FROM `CareCallRecord`
WHERE `health_status_new` IS NULL AND `health_status` IS NOT NULL;

ALTER TABLE `CareCallRecord`
    DROP COLUMN `health_status`,
    CHANGE COLUMN `health_status_new` `health_status` ENUM('GOOD','BAD') NULL;

-- CareCallRecord.psych_status를 ENUM 문자열로 전환
ALTER TABLE `CareCallRecord`
    ADD COLUMN `psych_status_new` ENUM('GOOD','BAD') NULL;

UPDATE `CareCallRecord`
SET `psych_status_new` = CASE `psych_status`
                            WHEN 1 THEN 'GOOD'
                            WHEN 0 THEN 'BAD'
                            ELSE NULL
    END;

SELECT COUNT(*) AS invalid_psych_status_rows
FROM `CareCallRecord`
WHERE `psych_status_new` IS NULL AND `psych_status` IS NOT NULL;

ALTER TABLE `CareCallRecord`
    DROP COLUMN `psych_status`,
    CHANGE COLUMN `psych_status_new` `psych_status` ENUM('GOOD','BAD') NULL;
