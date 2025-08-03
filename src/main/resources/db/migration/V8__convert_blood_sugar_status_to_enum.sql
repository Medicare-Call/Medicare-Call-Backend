-- BloodSugarRecord 테이블의 status 컬럼을 TINYINT에서 VARCHAR로 변경
-- 기존 데이터는 NULL로 설정 (애초에 기록되지 않고 있었음. 기존 값이 있었다면 별도 스크립트로 변환 처리해야 했을 것이다.)
ALTER TABLE BloodSugarRecord
ADD COLUMN status_new VARCHAR(20) DEFAULT NULL;

-- 기존 status 컬럼 삭제
ALTER TABLE BloodSugarRecord
DROP COLUMN status;

-- 새 status 컬럼 이름 변경
ALTER TABLE BloodSugarRecord
CHANGE COLUMN status_new status VARCHAR(20) DEFAULT NULL; 