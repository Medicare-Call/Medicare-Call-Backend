-- 1. Member_Elder 조인 테이블 생성
CREATE TABLE Member_Elder (
    member_elder_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guardian_id INT NOT NULL,
    elder_id INT NOT NULL,
    authority ENUM('MANAGE', 'VIEW') NOT NULL DEFAULT 'MANAGE',
    UNIQUE KEY UK_member_elder_guardian_elder (guardian_id, elder_id),
    KEY idx_member_elder_guardian (guardian_id),
    KEY idx_member_elder_elder (elder_id),
    CONSTRAINT FK_member_elder_guardian FOREIGN KEY (guardian_id) REFERENCES Member (id),
    CONSTRAINT FK_member_elder_elder FOREIGN KEY (elder_id) REFERENCES Elder (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. 기존 guardian_id 기반 데이터 백필
INSERT INTO Member_Elder (guardian_id, elder_id, authority)
SELECT guardian_id, id, 'MANAGE'
FROM Elder
WHERE guardian_id IS NOT NULL and status = 'ACTIVATED';
ON DUPLICATE KEY UPDATE authority = NEW.authority;

-- 3. Elder 테이블에서 guardian 컬럼 제거
ALTER TABLE Elder
    DROP FOREIGN KEY FK353978eh6ot2j5pg5js4m934b,
    DROP INDEX FK353978eh6ot2j5pg5js4m934b,
    DROP COLUMN guardian_id;