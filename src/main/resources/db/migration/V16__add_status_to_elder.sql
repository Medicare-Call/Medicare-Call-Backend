-- Elder 테이블에 status 컬럼 추가 (soft delete 지원)
ALTER TABLE Elder ADD COLUMN status ENUM('ACTIVATED', 'DELETED') NOT NULL DEFAULT 'ACTIVATED';

-- 기존 데이터의 status를 모두 ACTIVATED로 설정
UPDATE Elder SET status = 'ACTIVATED' WHERE status IS NULL OR status = '';

-- status 컬럼에 인덱스 추가
CREATE INDEX idx_elder_status ON Elder(status);
