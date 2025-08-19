-- 외래 키 제약 조건 비활성화
SET FOREIGN_KEY_CHECKS = 0;

-- 임시 테이블 생성하여 기존 데이터를 복제
CREATE TABLE TempMedicationSchedule AS SELECT * FROM MedicationSchedule;

-- 기존 MedicationSchedule 테이블 비우기
TRUNCATE TABLE MedicationSchedule;

-- schedule_time 컬럼 타입을 ENUM으로 변경
ALTER TABLE MedicationSchedule MODIFY COLUMN schedule_time ENUM('MORNING', 'LUNCH', 'DINNER');

-- 임시 테이블에서 데이터를 읽어와 새로운 형식으로 삽입
INSERT INTO MedicationSchedule (elder_id, name, schedule_time)
SELECT
    elder_id,
    name,
    'MORNING'
FROM TempMedicationSchedule
WHERE schedule_time LIKE '%MORNING%';

INSERT INTO MedicationSchedule (elder_id, name, schedule_time)
SELECT
    elder_id,
    name,
    'LUNCH'
FROM TempMedicationSchedule
WHERE schedule_time LIKE '%LUNCH%';

INSERT INTO MedicationSchedule (elder_id, name, schedule_time)
SELECT
    elder_id,
    name,
    'DINNER'
FROM TempMedicationSchedule
WHERE schedule_time LIKE '%DINNER%';

-- 임시 테이블 삭제
DROP TABLE TempMedicationSchedule;

-- 외래 키 제약 조건 다시 활성화
SET FOREIGN_KEY_CHECKS = 1;
