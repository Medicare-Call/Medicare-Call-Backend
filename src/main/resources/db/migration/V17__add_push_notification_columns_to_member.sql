-- 푸시 알림 설정 컬럼 추가
ALTER TABLE Member
    -- 전체 푸시 알림 설정
    ADD COLUMN push_all ENUM('ON','OFF') NOT NULL DEFAULT 'ON',
    -- 케어콜 완료 시 알림 설정
    ADD COLUMN push_carecall_completed ENUM('ON','OFF') NOT NULL DEFAULT 'ON',
    -- 건강 이상 징후 알림 설정
    ADD COLUMN push_health_alert ENUM('ON','OFF') NOT NULL DEFAULT 'ON',
    -- 케어콜 부재중 알림 설정
    ADD COLUMN push_carecall_missed ENUM('ON','OFF') NOT NULL DEFAULT 'ON';

