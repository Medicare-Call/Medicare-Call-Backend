-- MedicationSchedule, MedicationTakenRecord 테이블에 복약명을 저장하기 위한 새로운 컬럼 추가
ALTER TABLE MedicationSchedule ADD COLUMN name VARCHAR(255);
ALTER TABLE MedicationTakenRecord ADD COLUMN name VARCHAR(255);

-- Medication 테이블로부터 fk를 통해 name 데이터를 추출
UPDATE MedicationSchedule ms
SET name = (SELECT m.name FROM medication m WHERE m.id = ms.medication_id);

UPDATE MedicationTakenRecord mtr
SET name = (SELECT m.name FROM medication m WHERE m.id = mtr.medication_id);

-- NOT NULL 설정
ALTER TABLE MedicationSchedule MODIFY COLUMN name VARCHAR(255) NOT NULL;
ALTER TABLE MedicationTakenRecord MODIFY COLUMN name VARCHAR(255) NOT NULL;

-- 의존성 제거
ALTER TABLE MedicationSchedule DROP FOREIGN KEY FKou7fj2p6i0w2pkv2jbl07q0c6;
ALTER TABLE MedicationSchedule DROP COLUMN medication_id;

ALTER TABLE MedicationTakenRecord DROP FOREIGN KEY FKbvl9qu6xd8qovnpfxgofokvwx;
ALTER TABLE MedicationTakenRecord DROP COLUMN medication_id;

-- Medication 테이블 제거
DROP TABLE Medication;
