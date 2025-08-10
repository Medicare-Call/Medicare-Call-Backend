-- 주문 테이블 생성
CREATE TABLE Orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    product_name VARCHAR(128) NOT NULL,
    product_count INT NOT NULL,
    total_pay_amount INT NOT NULL,
    tax_scope_amount INT NOT NULL,
    tax_ex_scope_amount INT NOT NULL,
    naverpay_reserve_id VARCHAR(64),
    naverpay_payment_id VARCHAR(64),
    naverpay_hist_id VARCHAR(64),
    status ENUM('CREATED','PAYMENT_PENDING','PAYMENT_COMPLETED','PAYMENT_FAILED','CANCELLED') NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    approved_at DATETIME,
    member_id INT NOT NULL,
    elder_ids JSON,
    payment_method ENUM('NAVER_PAY') NOT NULL,
    FOREIGN KEY (member_id) REFERENCES Member(id)
);
