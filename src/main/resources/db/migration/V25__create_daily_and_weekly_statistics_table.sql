CREATE TABLE daily_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    elder_id INT NOT NULL,
    date DATE NOT NULL,
    ai_summary TEXT,
    breakfast_taken BOOLEAN,
    lunch_taken BOOLEAN,
    dinner_taken BOOLEAN,
    medication_total_taken INT,
    medication_total_goal INT,
    medication_details JSON,
    avg_sleep_minutes INT,
    health_status VARCHAR(255),
    mental_status VARCHAR(255),
    avg_blood_sugar INT,
    CONSTRAINT fk_daily_statistics_elder FOREIGN KEY (elder_id) REFERENCES elder (id),
    UNIQUE KEY uk_daily_statistics_elder_date (elder_id, date)
);

CREATE INDEX idx_daily_statistics_elder_id_date ON daily_statistics (elder_id, date);

CREATE TABLE weekly_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    elder_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    ai_health_summary TEXT,
    meal_rate INT,
    medication_rate INT,
    health_signals INT,
    missed_calls INT,
    breakfast_count INT,
    lunch_count INT,
    dinner_count INT,
    medication_stats JSON,
    avg_sleep_hours INT,
    avg_sleep_minutes INT,
    psych_good_count INT,
    psych_normal_count INT,
    psych_bad_count INT,
    blood_sugar_stats JSON,
    CONSTRAINT fk_weekly_statistics_elder FOREIGN KEY (elder_id) REFERENCES elder (id),
    UNIQUE KEY uk_weekly_statistics_elder_start_date (elder_id, start_date)
);

CREATE INDEX idx_weekly_statistics_elder_id_dates ON weekly_statistics (elder_id, start_date, end_date);
