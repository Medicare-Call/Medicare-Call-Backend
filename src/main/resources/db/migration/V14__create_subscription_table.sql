-- 구독 테이블 생성
CREATE TABLE Subscription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    elder_id INT NOT NULL,
    plan ENUM('STANDARD', 'PREMIUM') NOT NULL,
    price INT NOT NULL,
    status ENUM('ACTIVE', 'CANCELED', 'PAUSED') NOT NULL,
    start_date DATE NOT NULL,
    next_billing_date DATE NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (member_id) REFERENCES Member(id),
    FOREIGN KEY (elder_id) REFERENCES Elder(id),
    UNIQUE (elder_id)
);
