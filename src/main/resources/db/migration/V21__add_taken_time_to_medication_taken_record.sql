ALTER TABLE MedicationTakenRecord
ADD COLUMN taken_time ENUM('MORNING', 'LUNCH', 'DINNER');
