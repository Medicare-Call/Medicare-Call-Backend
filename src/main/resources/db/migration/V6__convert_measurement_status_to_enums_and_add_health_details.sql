-- BloodSugarRecord 테이블의 measurement_type 컬럼을 TINYINT에서 VARCHAR로 변경
-- 기존 데이터 변환: 1 -> 'BEFORE_MEAL', 2 -> 'AFTER_MEAL'
ALTER TABLE BloodSugarRecord 
ADD COLUMN measurement_type_new VARCHAR(20) DEFAULT NULL;

UPDATE BloodSugarRecord 
SET measurement_type_new = CASE 
    WHEN measurement_type = 1 THEN 'BEFORE_MEAL'
    WHEN measurement_type = 2 THEN 'AFTER_MEAL'
    ELSE NULL
END;

ALTER TABLE BloodSugarRecord 
DROP COLUMN measurement_type;

ALTER TABLE BloodSugarRecord 
CHANGE COLUMN measurement_type_new measurement_type VARCHAR(20) DEFAULT NULL;

-- MedicationTakenRecord 테이블의 taken_status 컬럼을 TINYINT에서 VARCHAR로 변경
-- 기존 데이터 변환: 1 -> 'TAKEN', 0 -> 'NOT_TAKEN'
ALTER TABLE MedicationTakenRecord 
ADD COLUMN taken_status_new VARCHAR(20) DEFAULT NULL;

UPDATE MedicationTakenRecord 
SET taken_status_new = CASE 
    WHEN taken_status = 1 THEN 'TAKEN'
    WHEN taken_status = 0 THEN 'NOT_TAKEN'
    ELSE NULL
END;

ALTER TABLE MedicationTakenRecord 
DROP COLUMN taken_status;

ALTER TABLE MedicationTakenRecord 
CHANGE COLUMN taken_status_new taken_status VARCHAR(20) DEFAULT NULL;

-- CareCallRecord 테이블에 새로운 컬럼 추가
ALTER TABLE CareCallRecord 
ADD COLUMN psychological_details TEXT DEFAULT NULL;

ALTER TABLE CareCallRecord 
ADD COLUMN health_details TEXT DEFAULT NULL; 