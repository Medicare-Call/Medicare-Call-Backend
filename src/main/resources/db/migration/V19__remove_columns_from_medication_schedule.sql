-- 미사용 컬럼 제거
ALTER TABLE MedicationSchedule
    DROP COLUMN dosage,
    DROP COLUMN frequency_detail,
    DROP COLUMN notes;
