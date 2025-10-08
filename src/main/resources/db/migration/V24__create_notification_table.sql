CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    title VARCHAR(120),
    body VARCHAR(1000),
    created_at DATETIME NOT NULL,
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_notification_member
        FOREIGN KEY (member_id)
            REFERENCES member(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

CREATE INDEX idx_member_read_status ON notification (member_id, is_read);
