CREATE TABLE `BloodSugarRecord` (
  `blood_sugar_value` decimal(38,2) DEFAULT NULL,
  `carecall_record_id` int NOT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `measurement_type` tinyint DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `recorded_at` datetime(6) NOT NULL,
  `unit` varchar(10) DEFAULT NULL,
  `response_summary` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqy3ub514tpniofrjvxsw42qwk` (`carecall_record_id`),
  CONSTRAINT `FKqy3ub514tpniofrjvxsw42qwk` FOREIGN KEY (`carecall_record_id`) REFERENCES `CareCallRecord` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `CareCallRecord` (
  `elder_id` int NOT NULL,
  `health_status` tinyint DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `psych_status` tinyint DEFAULT NULL,
  `responded` tinyint NOT NULL,
  `setting_id` int NOT NULL,
  `called_at` datetime(6) NOT NULL,
  `sleep_end` datetime(6) DEFAULT NULL,
  `sleep_start` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK22eop43omeh3f5r8fsxdrgvs6` (`elder_id`),
  KEY `FKeetkj0f9vop32qe5pckauuw9x` (`setting_id`),
  CONSTRAINT `FK22eop43omeh3f5r8fsxdrgvs6` FOREIGN KEY (`elder_id`) REFERENCES `Elder` (`id`),
  CONSTRAINT `FKeetkj0f9vop32qe5pckauuw9x` FOREIGN KEY (`setting_id`) REFERENCES `CareCallSetting` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `CareCallSetting` (
  `elder_id` int NOT NULL,
  `first_call_time` time(6) NOT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `recurrence` tinyint NOT NULL,
  `second_call_time` time(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_lvsm78wepwiv6hfp7q6sr0iuw` (`elder_id`),
  CONSTRAINT `FKrtgvsbfsy02g0x3458vdl6vqt` FOREIGN KEY (`elder_id`) REFERENCES `Elder` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `Disease` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `description` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_s3rk1etrum3h7ou4w6dhvbmem` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `Elder` (
  `birth_date` date DEFAULT NULL,
  `gender` tinyint NOT NULL,
  `guardian_id` int NOT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `relationship` enum('GRANDCHILD','SPOUSE') NOT NULL,
  `residence_type` enum('ALONE','WITH_SPOUSE','WITH_ME','WITH_FAMILY') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK353978eh6ot2j5pg5js4m934b` (`guardian_id`),
  CONSTRAINT `FK353978eh6ot2j5pg5js4m934b` FOREIGN KEY (`guardian_id`) REFERENCES `Member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ElderDisease` (
  `disease_id` int NOT NULL,
  `elder_id` int NOT NULL,
  PRIMARY KEY (`disease_id`,`elder_id`),
  KEY `FKk5pdvexqecs7e703qsvpetu0w` (`elder_id`),
  CONSTRAINT `FK454rfteyamlk3dc0f9uf2j4s0` FOREIGN KEY (`disease_id`) REFERENCES `Disease` (`id`),
  CONSTRAINT `FKk5pdvexqecs7e703qsvpetu0w` FOREIGN KEY (`elder_id`) REFERENCES `Elder` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ElderHealthInfo` (
  `elder_id` int NOT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `notes` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_36o1u7898hkcdf69l0xrk41lp` (`elder_id`),
  CONSTRAINT `FKaf0389yptk78n9vv8gevq50x3` FOREIGN KEY (`elder_id`) REFERENCES `Elder` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `GuardianSettings` (
  `call_complete_alert` tinyint NOT NULL DEFAULT '0',
  `guardian_id` int NOT NULL,
  `health_alert` tinyint NOT NULL DEFAULT '0',
  `id` int NOT NULL AUTO_INCREMENT,
  `missed_call_alert` tinyint NOT NULL DEFAULT '0',
  `push_alert` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_luo9bapil2vx04relahyg8rhi` (`guardian_id`),
  CONSTRAINT `FKdg9r7yykxnvajk22wfey7dfmj` FOREIGN KEY (`guardian_id`) REFERENCES `Member` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `MealRecord` (
  `carecall_record_id` int NOT NULL,
  `eaten_status` tinyint DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `meal_type` tinyint DEFAULT NULL,
  `recorded_at` datetime(6) NOT NULL,
  `response_summary` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5872ct944a5pm1jqpaobhc5my` (`carecall_record_id`),
  CONSTRAINT `FK5872ct944a5pm1jqpaobhc5my` FOREIGN KEY (`carecall_record_id`) REFERENCES `CareCallRecord` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `Medication` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `MedicationSchedule` (
  `elder_id` int NOT NULL,
  `frequency_type` tinyint NOT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `medication_id` int NOT NULL,
  `schedule_time` time(6) NOT NULL,
  `dosage` varchar(50) DEFAULT NULL,
  `frequency_detail` varchar(100) DEFAULT NULL,
  `notes` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1sgwf6xgmnvrivk1gk7tkuk9r` (`elder_id`),
  KEY `FKou7fj2p6i0w2pkv2jbl07q0c6` (`medication_id`),
  CONSTRAINT `FK1sgwf6xgmnvrivk1gk7tkuk9r` FOREIGN KEY (`elder_id`) REFERENCES `Elder` (`id`),
  CONSTRAINT `FKou7fj2p6i0w2pkv2jbl07q0c6` FOREIGN KEY (`medication_id`) REFERENCES `Medication` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `MedicationTakenRecord` (
  `carecall_record_id` int NOT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `medication_id` int NOT NULL,
  `medication_schedule_id` int DEFAULT NULL,
  `taken_status` tinyint DEFAULT NULL,
  `recorded_at` datetime(6) NOT NULL,
  `response_summary` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjgswjk7jgi31ksf4a02dt8bjc` (`carecall_record_id`),
  KEY `FKbvl9qu6xd8qovnpfxgofokvwx` (`medication_id`),
  KEY `FKfnfuy2mhtnv872da46owxnbf0` (`medication_schedule_id`),
  CONSTRAINT `FKbvl9qu6xd8qovnpfxgofokvwx` FOREIGN KEY (`medication_id`) REFERENCES `Medication` (`id`),
  CONSTRAINT `FKfnfuy2mhtnv872da46owxnbf0` FOREIGN KEY (`medication_schedule_id`) REFERENCES `MedicationSchedule` (`id`),
  CONSTRAINT `FKjgswjk7jgi31ksf4a02dt8bjc` FOREIGN KEY (`carecall_record_id`) REFERENCES `CareCallRecord` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `Member` (
  `birth_date` date DEFAULT NULL,
  `gender` tinyint NOT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `plan` tinyint NOT NULL,
  `terms_agreed_at` datetime(6) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `WeeklyStats` (
  `absent_count` int NOT NULL,
  `avg_sleep_hours` time(6) NOT NULL,
  `elder_id` int NOT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `week_end` date NOT NULL,
  `week_start` date NOT NULL,
  `blood_sugar_stats` json DEFAULT NULL,
  `health_summary` text,
  `meal_rate` json NOT NULL,
  `medication_rate` json NOT NULL,
  `psych_summary` json DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlw365wllj7kap43pqgfhaqrib` (`elder_id`),
  CONSTRAINT `FKlw365wllj7kap43pqgfhaqrib` FOREIGN KEY (`elder_id`) REFERENCES `Elder` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci; 