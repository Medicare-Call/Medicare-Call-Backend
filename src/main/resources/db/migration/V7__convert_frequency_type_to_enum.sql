-- MedicationSchedule 테이블의 frequency_type 컬럼을 TINYINT에서 VARCHAR로 변경
-- 기존 데이터 변환: 0 -> 'DAILY', 1 -> 'WEEKLY', 2 -> 'MONTHLY', 3 -> 'SPECIFIC_DAYS'
ALTER TABLE MedicationSchedule 
ADD COLUMN frequency_type_new VARCHAR(20) DEFAULT NULL;

UPDATE MedicationSchedule 
SET frequency_type_new = CASE 
    WHEN frequency_type = 0 THEN 'DAILY'
    WHEN frequency_type = 1 THEN 'WEEKLY'
    WHEN frequency_type = 2 THEN 'MONTHLY'
    WHEN frequency_type = 3 THEN 'SPECIFIC_DAYS'
    ELSE 'DAILY'
END;

ALTER TABLE MedicationSchedule 
DROP COLUMN frequency_type;

ALTER TABLE MedicationSchedule 
CHANGE COLUMN frequency_type_new frequency_type VARCHAR(20) NOT NULL; 