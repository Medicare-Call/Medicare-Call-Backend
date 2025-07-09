-- ---------------------------------------------------
-- Schema for Care Call Application
-- ---------------------------------------------------

CREATE TABLE IF NOT EXISTS `Member` (
  `id`               INT           NOT NULL AUTO_INCREMENT,
  `name`             VARCHAR(100)  NOT NULL,
  `phone`            VARCHAR(20)   NOT NULL,
  `birth_date`       DATE,
  `gender`           TINYINT       NOT NULL COMMENT '0: M, 1: F',
  `terms_agreed_at`  DATETIME      NOT NULL,
  `plan`             TINYINT       NOT NULL COMMENT '0: BASIC, 1: PREMIUM',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='가입한 회원에 대한 데이터';

CREATE TABLE IF NOT EXISTS `Elder` (
  `id`              INT           NOT NULL AUTO_INCREMENT,
  `guardian_id`     INT           NOT NULL,
  `name`            VARCHAR(100)  NOT NULL,
  `birth_date`      DATE,
  `gender`          TINYINT       NOT NULL COMMENT '0: M, 1: F',
  `phone`           VARCHAR(20),
  `relationship`    TINYINT       NOT NULL COMMENT '0: CHILD, 1: SPOUSE, 2: OTHER',
  `residence_type`  TINYINT       NOT NULL COMMENT '0: HOME, 1: CARE_FACILITY, 2: OTHER',
  PRIMARY KEY (`id`),
  INDEX `idx_elder_guardian` (`guardian_id`),
  CONSTRAINT `fk_elder_guardian`
      FOREIGN KEY (`guardian_id`) REFERENCES `Member`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='회원이 관리하는 어르신 데이터';

-- ---------------------------------------------------
-- Health & Medication Information
-- ---------------------------------------------------

CREATE TABLE IF NOT EXISTS `Disease` (
  `id`          INT           NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(200)  NOT NULL UNIQUE,
  `description` TEXT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='질병 데이터 (공기관에서 가져온 데이터를 사용한다고 들었음)';

CREATE TABLE IF NOT EXISTS `Medication` (
  `id`   INT           NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200)  NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='약 데이터 (이것도 가져와야 할지 검토)';

CREATE TABLE IF NOT EXISTS `ElderDisease` (
  `elder_id`   INT NOT NULL,
  `disease_id` INT NOT NULL,
  PRIMARY KEY (`elder_id`,`disease_id`),
  CONSTRAINT `fk_elderdisease_elder`
      FOREIGN KEY (`elder_id`) REFERENCES `Elder`(`id`),
  CONSTRAINT `fk_elderdisease_disease`
      FOREIGN KEY (`disease_id`) REFERENCES `Disease`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='질병 실데이터';

CREATE TABLE IF NOT EXISTS `MedicationSchedule` (
  `id`               INT           NOT NULL AUTO_INCREMENT,
  `elder_id`         INT           NOT NULL,
  `medication_id`    INT           NOT NULL,
  `dosage`           VARCHAR(50),
  `schedule_time`    TIME          NOT NULL,
  `frequency_type`   TINYINT       NOT NULL COMMENT '0: DAILY, 1: WEEKLY, 2: MONTHLY, 3: SPECIFIC_DAYS',
  `frequency_detail` VARCHAR(100),
  `notes`            VARCHAR(500),
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_medschedule_elder`
      FOREIGN KEY (`elder_id`) REFERENCES `Elder`(`id`),
  CONSTRAINT `fk_medschedule_medication`
      FOREIGN KEY (`medication_id`) REFERENCES `Medication`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='복약 예정 스케줄 데이터';

CREATE TABLE IF NOT EXISTS `ElderHealthInfo` (
  `id`        INT   NOT NULL AUTO_INCREMENT,
  `elder_id`  INT   NOT NULL UNIQUE,
  `notes`     TEXT,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_elderhealthinfo_elder`
      FOREIGN KEY (`elder_id`) REFERENCES `Elder`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='어르신 특이사항 데이터';

-- ---------------------------------------------------
-- Care Call Management
-- ---------------------------------------------------

CREATE TABLE IF NOT EXISTS `CareCallSetting` (
  `id`              INT      NOT NULL AUTO_INCREMENT,
  `elder_id`        INT      NOT NULL UNIQUE,
  `first_call_time` TIME     NOT NULL,
  `second_call_time` TIME,
  `recurrence`      TINYINT  NOT NULL COMMENT '0: DAILY, 1: WEEKLY, 2: MONTHLY',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_carecallsetting_elder`
      FOREIGN KEY (`elder_id`) REFERENCES `Elder`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='케어콜 설정';

CREATE TABLE IF NOT EXISTS `CareCallRecord` (
  `id`            INT      NOT NULL AUTO_INCREMENT,
  `elder_id`      INT      NOT NULL,
  `setting_id`    INT      NOT NULL,
  `called_at`     DATETIME NOT NULL,
  `responded`     TINYINT  NOT NULL COMMENT '0: N, 1: Y',
  `sleep_start`   DATETIME,
  `sleep_end`     DATETIME,
  `health_status` TINYINT  COMMENT '0: GOOD, 1: NORMAL, 2: BAD',
  `psych_status`  TINYINT  COMMENT '0: GOOD, 1: NORMAL, 2: BAD',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_carecallrecord_elder`
      FOREIGN KEY (`elder_id`) REFERENCES `Elder`(`id`),
  CONSTRAINT `fk_carecallrecord_setting`
      FOREIGN KEY (`setting_id`) REFERENCES `CareCallSetting`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='각 케어콜별 응답 데이터';

-- ---------------------------------------------------
-- Detailed Care Call Records
-- ---------------------------------------------------

CREATE TABLE IF NOT EXISTS `MealRecord` (
  `id`                 INT      NOT NULL AUTO_INCREMENT,
  `carecall_record_id` INT      NOT NULL,
  `meal_type`          TINYINT  COMMENT '0: BREAKFAST, 1: LUNCH, 2: DINNER, 3: SNACK',
  `eaten_status`       TINYINT  COMMENT '0: FULLY, 1: PARTIALLY, 2: NOT_EATEN',
  `response_summary`   VARCHAR(500),
  `recorded_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_mealrecord_carecall`
      FOREIGN KEY (`carecall_record_id`) REFERENCES `CareCallRecord`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='식사 실데이터';

CREATE TABLE IF NOT EXISTS `MedicationTakenRecord` (
  `id`                     INT      NOT NULL AUTO_INCREMENT,
  `carecall_record_id`     INT      NOT NULL,
  `medication_schedule_id` INT,
  `medication_id`          INT      NOT NULL,
  `taken_status`           TINYINT  COMMENT '0: TAKEN, 1: NOT_TAKEN, 2: PARTIALLY_TAKEN',
  `response_summary`       VARCHAR(500),
  `recorded_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_medtakenrecord_carecall`
      FOREIGN KEY (`carecall_record_id`) REFERENCES `CareCallRecord`(`id`),
  CONSTRAINT `fk_medtakenrecord_schedule`
      FOREIGN KEY (`medication_schedule_id`) REFERENCES `MedicationSchedule`(`id`),
  CONSTRAINT `fk_medtakenrecord_medication`
      FOREIGN KEY (`medication_id`) REFERENCES `Medication`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='복약 실데이터';

CREATE TABLE IF NOT EXISTS `BloodSugarRecord` (
  `id`                   INT          NOT NULL AUTO_INCREMENT,
  `carecall_record_id`   INT          NOT NULL,
  `measurement_type`     TINYINT      COMMENT '0: FASTING, 1: POSTPRANDIAL, 2: BEFORE_MEAL, 3: OTHER',
  `value`                DECIMAL(5,2),
  `unit`                 VARCHAR(10)  DEFAULT 'mg/dL',
  `status`               TINYINT      COMMENT '0: NORMAL, 1: HIGH, 2: LOW, 3: UNKNOWN',
  `recorded_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `response_summary`     VARCHAR(500),
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_bloodsugar_carecall`
      FOREIGN KEY (`carecall_record_id`) REFERENCES `CareCallRecord`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='혈당 실데이터';

-- ---------------------------------------------------
-- Statistics & Settings
-- ---------------------------------------------------

CREATE TABLE IF NOT EXISTS `WeeklyStats` (
  `id`                INT      NOT NULL AUTO_INCREMENT,
  `elder_id`          INT      NOT NULL,
  `week_start`        DATE     NOT NULL,
  `week_end`          DATE     NOT NULL,
  `meal_rate`         JSON     NOT NULL,
  `medication_rate`   JSON     NOT NULL,
  `absent_count`      INT      NOT NULL,
  `avg_sleep_hours`   TIME     NOT NULL,
  `blood_sugar_stats` JSON,
  `health_summary`    TEXT,
  `psych_summary`     JSON,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_weeklystats_elder_week` (`elder_id`, `week_start`),
  CONSTRAINT `fk_weeklystats_elder`
      FOREIGN KEY (`elder_id`) REFERENCES `Elder`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='주간 분석 대시보드 데이터';

CREATE TABLE IF NOT EXISTS `GuardianSettings` (
  `id`                  INT      NOT NULL AUTO_INCREMENT,
  `guardian_id`         INT      NOT NULL UNIQUE,
  `push_alert`          TINYINT  NOT NULL DEFAULT 0 COMMENT '0: ON, 1: OFF',
  `call_complete_alert` TINYINT  NOT NULL DEFAULT 0 COMMENT '0: ON, 1: OFF',
  `health_alert`        TINYINT  NOT NULL DEFAULT 0 COMMENT '0: ON, 1: OFF',
  `missed_call_alert`   TINYINT  NOT NULL DEFAULT 0 COMMENT '0: ON, 1: OFF',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_guardiansettings_guardian`
      FOREIGN KEY (`guardian_id`) REFERENCES `Member`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='보호자 어플리케이션 설정'; 