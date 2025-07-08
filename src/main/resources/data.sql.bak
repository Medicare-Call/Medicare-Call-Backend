-- 데이터 초기화
DELETE FROM GuardianSettings;
DELETE FROM WeeklyStats;
DELETE FROM BloodSugarRecord;
DELETE FROM MedicationTakenRecord;
DELETE FROM MealRecord;
DELETE FROM CareCallRecord;
DELETE FROM CareCallSetting;
DELETE FROM ElderHealthInfo;
DELETE FROM MedicationSchedule;
DELETE FROM ElderDisease;
DELETE FROM Medication;
DELETE FROM Disease;
DELETE FROM Elder;
DELETE FROM Member;

-- ───────────────────────────────────────────────────────────────
-- Members
-- ───────────────────────────────────────────────────────────────
INSERT INTO `Member` (id, name, phone, birth_date, gender, terms_agreed_at, plan) VALUES
                                                                                      (1, '김철수',   '010-1234-5678', '1950-05-15', 0, '2025-01-01 10:00:00', 1),
                                                                                      (2, '박영희',   '010-8765-4321', '1945-12-20', 1, '2025-02-10 14:30:00', 0);

-- ───────────────────────────────────────────────────────────────
-- Elders
-- ───────────────────────────────────────────────────────────────
INSERT INTO `Elder` (id, guardian_id, name, birth_date, gender, phone, relationship, residence_type) VALUES
                                                                                                         (1, 1, '이순자', '1930-07-08', 1, '010-1111-2222', 2, 0),
                                                                                                         (2, 2, '최영수', '1935-03-22', 0, '010-3333-4444', 0, 1);

-- ───────────────────────────────────────────────────────────────
-- Diseases & Medications
-- ───────────────────────────────────────────────────────────────
INSERT INTO `Disease` (id, name, description) VALUES
                                                  (1, 'High Blood Pressure', '고혈압'),
                                                  (2, 'Diabetes',           '당뇨병');

INSERT INTO `Medication` (id, name) VALUES
                                        (1, 'Aspirin'),
                                        (2, 'Metformin');

-- ───────────────────────────────────────────────────────────────
-- Elder → Disease 연관
-- ───────────────────────────────────────────────────────────────
INSERT INTO `ElderDisease` (elder_id, disease_id) VALUES
                                                      (1, 1),
                                                      (1, 2),
                                                      (2, 1);

-- ───────────────────────────────────────────────────────────────
-- MedicationSchedule
-- ───────────────────────────────────────────────────────────────
INSERT INTO `MedicationSchedule`
(id, elder_id, medication_id, dosage, schedule_time, frequency_type, frequency_detail, notes) VALUES
                                                                                                  (1, 1, 1, '100mg', '08:00:00', 0, '',    '아침 식후'),
                                                                                                  (2, 1, 2, '500mg', '20:00:00', 0, '',    '저녁 식후'),
                                                                                                  (3, 2, 1, '100mg', '09:00:00', 1, 'Mon,Wed,Fri', '격일 복용');

-- ───────────────────────────────────────────────────────────────
-- ElderHealthInfo
-- ───────────────────────────────────────────────────────────────
INSERT INTO `ElderHealthInfo` (id, elder_id, notes) VALUES
                                                        (1, 1, '고혈압, 당뇨 관리 필요'),
                                                        (2, 2, '요통, 관절염 있음');

-- ───────────────────────────────────────────────────────────────
-- CareCallSetting
-- ───────────────────────────────────────────────────────────────
INSERT INTO `CareCallSetting` (id, elder_id, first_call_time, second_call_time, recurrence) VALUES
                                                                                                (1, 1, '09:00:00', '18:00:00', 0),
                                                                                                (2, 2, '10:00:00', NULL,      1);

-- ───────────────────────────────────────────────────────────────
-- CareCallRecord
-- ───────────────────────────────────────────────────────────────
INSERT INTO `CareCallRecord`
(id, elder_id, setting_id, called_at, responded, sleep_start, sleep_end, health_status, psych_status) VALUES
                                                                                                          (1, 1, 1, '2025-07-08 09:00:00', 1, '2025-07-08 22:30:00', '2025-07-09 06:30:00', 0, 1),
                                                                                                          (2, 2, 2, '2025-07-08 10:00:00', 0, NULL,              NULL,               NULL,  NULL);

-- ───────────────────────────────────────────────────────────────
-- MealRecord
-- ───────────────────────────────────────────────────────────────
INSERT INTO `MealRecord`
(id, carecall_record_id, meal_type, eaten_status, response_summary, recorded_at) VALUES
                                                                                     (1, 1, 0, 0, '잘 먹음',     '2025-07-08 09:05:00'),
                                                                                     (2, 1, 1, 1, '조금만 먹음', '2025-07-08 12:05:00'),
                                                                                     (3, 1, 2, 2, '식사 거부',   '2025-07-08 18:05:00');

-- ───────────────────────────────────────────────────────────────
-- MedicationTakenRecord
-- ───────────────────────────────────────────────────────────────
INSERT INTO `MedicationTakenRecord`
(id, carecall_record_id, medication_schedule_id, medication_id, taken_status, response_summary, recorded_at) VALUES
                                                                                                                 (1, 1, 1, 1, 0, '정상 복약',    '2025-07-08 08:05:00'),
                                                                                                                 (2, 1, 2, 2, 1, '미복약',       '2025-07-08 20:05:00'),
                                                                                                                 (3, 2, 3, 1, 2, '반만 복약',    '2025-07-08 09:05:00');

-- ───────────────────────────────────────────────────────────────
-- BloodSugarRecord
-- ───────────────────────────────────────────────────────────────
INSERT INTO `BloodSugarRecord`
(id, carecall_record_id, measurement_type, value, unit, status, recorded_at,      response_summary) VALUES
                                                                                                        (1, 1, 0, 95.50, 'mg/dL', 0, '2025-07-08 09:10:00', '정상 범위'),
                                                                                                        (2, 1, 1,140.75, 'mg/dL', 1, '2025-07-08 14:10:00', '다소 높음'),
                                                                                                        (3, 2, 0,110.00, 'mg/dL', 0, '2025-07-08 10:10:00', '정상');

-- ───────────────────────────────────────────────────────────────
-- WeeklyStats
-- ───────────────────────────────────────────────────────────────
INSERT INTO `WeeklyStats`
(id, elder_id, week_start, week_end, meal_rate, medication_rate,
 absent_count, avg_sleep_hours, blood_sugar_stats, health_summary, psych_summary) VALUES
                                                                                      (1, 1, '2025-06-30', '2025-07-06',
                                                                                       '{"breakfast":100,"lunch":100,"dinner":67}',
                                                                                       '{"Aspirin":100,"Metformin":50}',
                                                                                       1, '08:00:00',
                                                                                       '{"mean":115.42,"max":140.75,"min":95.5}',
                                                                                       '양호',
                                                                                       '{"mood":"stable"}'),
                                                                                      (2, 2, '2025-06-30', '2025-07-06',
                                                                                       '{"breakfast":50,"lunch":0,"dinner":0}',
                                                                                       '{"Aspirin":33}',
                                                                                       2, '07:45:00',
                                                                                       '{"mean":110.0,"max":110.0,"min":110.0}',
                                                                                       '보통',
                                                                                       '{"mood":"anxious"}');

-- ───────────────────────────────────────────────────────────────
-- GuardianSettings
-- ───────────────────────────────────────────────────────────────
INSERT INTO `GuardianSettings`
(id, guardian_id, push_alert, call_complete_alert, health_alert, missed_call_alert) VALUES
                                                                                        (1, 1, 0, 0, 1, 0),
                                                                                        (2, 2, 1, 0, 1, 1); 