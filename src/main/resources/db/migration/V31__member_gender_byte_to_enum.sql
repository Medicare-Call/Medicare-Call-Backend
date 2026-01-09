-- 1) 새 ENUM 컬럼 추가
ALTER TABLE `Member`
    ADD COLUMN `gender_new` ENUM('MALE','FEMALE') NULL;

-- 2) 기존 0/1 값을 문자열 ENUM으로 백필
UPDATE `Member`
SET `gender_new` = CASE `gender`
                       WHEN 0 THEN 'MALE'
                       WHEN 1 THEN 'FEMALE'
                       ELSE NULL
    END;

-- 3) 변환 실패(=NULL) 체크
SELECT COUNT(*) AS invalid_rows
FROM `Member`
WHERE `gender_new` IS NULL;

-- invalid_rows = 0 확인 후 4) 스왑 (기존 컬럼 제거 + 새 컬럼을 gender로)
ALTER TABLE `Member`
DROP COLUMN `gender`,
  CHANGE COLUMN `gender_new` `gender` ENUM('MALE','FEMALE') NOT NULL;
