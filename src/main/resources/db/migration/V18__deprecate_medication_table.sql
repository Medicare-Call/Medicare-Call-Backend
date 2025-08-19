-- MedicationSchedule, MedicationTakenRecord 테이블에 복약명을 저장하기 위한 새로운 컬럼 추가
ALTER TABLE MedicationSchedule ADD COLUMN name VARCHAR(255);
ALTER TABLE MedicationTakenRecord ADD COLUMN name VARCHAR(255);

-- Medication 테이블에서 fk로 연결되어 있으니
UPDATE MedicationSchedule ms
SET name = (SELECT m.name FROM medication m WHERE m.id = ms.medication_id);

UPDATE MedicationTakenRecord mtr
SET name = (SELECT m.name FROM medication m WHERE m.id = mtr.medication_id);

-- Set the new 'name' columns to be NOT NULL
ALTER TABLE MedicationSchedule ALTER COLUMN name SET NOT NULL;
ALTER TABLE MedicationTakenRecord ALTER COLUMN name SET NOT NULL;

-- Drop foreign key constraints and the medication_id column
-- Note: Constraint names can vary. Replace with actual names if different.
-- First, find the constraint names if you don't know them:
-- SELECT conname FROM pg_constraint WHERE conrelid = 'MedicationSchedule'::regclass AND confrelid = 'medication'::regclass;
-- SELECT conname FROM pg_constraint WHERE conrelid = 'MedicationTakenRecord'::regclass AND confrelid = 'medication'::regclass;

-- Assuming default or known constraint names, drop them.
-- You might need to adjust these names based on your actual schema.
ALTER TABLE MedicationSchedule DROP CONSTRAINT IF EXISTS MedicationSchedule_medication_id_fkey;
ALTER TABLE MedicationSchedule DROP COLUMN medication_id;

ALTER TABLE MedicationTakenRecord DROP CONSTRAINT IF EXISTS MedicationTakenRecord_medication_id_fkey;
ALTER TABLE MedicationTakenRecord DROP COLUMN medication_id;


-- Drop the now-redundant medication table
DROP TABLE medication;
