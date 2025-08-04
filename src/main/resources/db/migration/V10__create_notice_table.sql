-- 공지사항 테이블 생성
CREATE TABLE notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL,
    contents TEXT NOT NULL,
    published_at DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 샘플 데이터 삽입
INSERT INTO notice (title, author, contents, published_at) VALUES
('메디케어콜 서비스 안내', '관리자', '안녕하세요. 메디케어콜 서비스입니다. 많은 이용 부탁드립니다.', '2025-07-01');