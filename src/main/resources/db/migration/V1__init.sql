CREATE TABLE Disease (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(200) NOT NULL,
                         description TEXT,
                         CONSTRAINT UK_s3rk1etrum3h7ou4w6dhvbmem UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE Medication (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(200) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE Member (
                        birth_date DATE DEFAULT NULL,
                        gender TINYINT NOT NULL,
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        plan TINYINT NOT NULL,
                        terms_agreed_at DATETIME(6) NOT NULL,
                        phone VARCHAR(20) NOT NULL,
                        name VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE Elder (
                       birth_date DATE DEFAULT NULL,
                       gender TINYINT NOT NULL,
                       guardian_id INT NOT NULL,
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       phone VARCHAR(20) DEFAULT NULL,
                       name VARCHAR(100) NOT NULL,
                       relationship ENUM('GRANDCHILD','SPOUSE') NOT NULL,
                       residence_type ENUM('ALONE','WITH_SPOUSE','WITH_ME','WITH_FAMILY') NOT NULL,
                       KEY FK353978eh6ot2j5pg5js4m934b (guardian_id),
                       CONSTRAINT FK353978eh6ot2j5pg5js4m934b FOREIGN KEY (guardian_id) REFERENCES Member (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE CareCallSetting (
                                 elder_id INT NOT NULL,
                                 first_call_time TIME(6) NOT NULL,
                                 id INT AUTO_INCREMENT PRIMARY KEY,
                                 recurrence TINYINT NOT NULL,
                                 second_call_time TIME(6) DEFAULT NULL,
                                 UNIQUE KEY UK_lvsm78wepwiv6hfp7q6sr0iuw (elder_id),
                                 CONSTRAINT FKrtgvsbfsy02g0x3458vdl6vqt FOREIGN KEY (elder_id) REFERENCES Elder (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE MedicationSchedule (
                                    elder_id INT NOT NULL,
                                    frequency_type TINYINT NOT NULL,
                                    id INT AUTO_INCREMENT PRIMARY KEY,
                                    medication_id INT NOT NULL,
                                    schedule_time TIME(6) NOT NULL,
                                    dosage VARCHAR(50) DEFAULT NULL,
                                    frequency_detail VARCHAR(100) DEFAULT NULL,
                                    notes VARCHAR(500) DEFAULT NULL,
                                    KEY FK1sgwf6xgmnvrivk1gk7tkuk9r (elder_id),
                                    KEY FKou7fj2p6i0w2pkv2jbl07q0c6 (medication_id),
                                    CONSTRAINT FK1sgwf6xgmnvrivk1gk7tkuk9r FOREIGN KEY (elder_id) REFERENCES Elder (id),
                                    CONSTRAINT FKou7fj2p6i0w2pkv2jbl07q0c6 FOREIGN KEY (medication_id) REFERENCES Medication (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE CareCallRecord (
                                elder_id INT NOT NULL,
                                health_status TINYINT DEFAULT NULL,
                                id INT AUTO_INCREMENT PRIMARY KEY,
                                psych_status TINYINT DEFAULT NULL,
                                responded TINYINT NOT NULL,
                                setting_id INT NOT NULL,
                                called_at DATETIME(6) NOT NULL,
                                sleep_end DATETIME(6) DEFAULT NULL,
                                sleep_start DATETIME(6) DEFAULT NULL,
                                KEY FK22eop43omeh3f5r8fsxdrgvs6 (elder_id),
                                KEY FKeetkj0f9vop32qe5pckauuw9x (setting_id),
                                CONSTRAINT FK22eop43omeh3f5r8fsxdrgvs6 FOREIGN KEY (elder_id) REFERENCES Elder (id),
                                CONSTRAINT FKeetkj0f9vop32qe5pckauuw9x FOREIGN KEY (setting_id) REFERENCES CareCallSetting (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ElderDisease (
                              disease_id INT NOT NULL,
                              elder_id INT NOT NULL,
                              PRIMARY KEY (disease_id, elder_id),
                              KEY FKk5pdvexqecs7e703qsvpetu0w (elder_id),
                              CONSTRAINT FK454rfteyamlk3dc0f9uf2j4s0 FOREIGN KEY (disease_id) REFERENCES Disease (id),
                              CONSTRAINT FKk5pdvexqecs7e703qsvpetu0w FOREIGN KEY (elder_id) REFERENCES Elder (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE ElderHealthInfo (
                                 elder_id INT NOT NULL,
                                 id INT AUTO_INCREMENT PRIMARY KEY,
                                 notes TEXT DEFAULT NULL,
                                 UNIQUE KEY UK_36o1u7898hkcdf69l0xrk41lp (elder_id),
                                 CONSTRAINT FKaf0389yptk78n9vv8gevq50x3 FOREIGN KEY (elder_id) REFERENCES Elder (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE GuardianSettings (
                                  call_complete_alert TINYINT NOT NULL DEFAULT 0,
                                  guardian_id INT NOT NULL,
                                  health_alert TINYINT NOT NULL DEFAULT 0,
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  missed_call_alert TINYINT NOT NULL DEFAULT 0,
                                  push_alert TINYINT NOT NULL DEFAULT 0,
                                  UNIQUE KEY UK_luo9bapil2vx04relahyg8rhi (guardian_id),
                                  CONSTRAINT FKdg9r7yykxnvajk22wfey7dfmj FOREIGN KEY (guardian_id) REFERENCES Member (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE MealRecord (
                            carecall_record_id INT NOT NULL,
                            eaten_status TINYINT DEFAULT NULL,
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            meal_type TINYINT DEFAULT NULL,
                            recorded_at DATETIME(6) NOT NULL,
                            response_summary VARCHAR(500) DEFAULT NULL,
                            KEY FK5872ct944a5pm1jqpaobhc5my (carecall_record_id),
                            CONSTRAINT FK5872ct944a5pm1jqpaobhc5my FOREIGN KEY (carecall_record_id) REFERENCES CareCallRecord (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE BloodSugarRecord (
                                  blood_sugar_value DECIMAL(38,2) DEFAULT NULL,
                                  carecall_record_id INT NOT NULL,
                                  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                  measurement_type TINYINT DEFAULT NULL,
                                  status TINYINT DEFAULT NULL,
                                  recorded_at DATETIME(6) NOT NULL,
                                  unit VARCHAR(10) DEFAULT NULL,
                                  response_summary VARCHAR(500) DEFAULT NULL,
                                  KEY FKqy3ub514tpniofrjvxsw42qwk (carecall_record_id),
                                  CONSTRAINT FKqy3ub514tpniofrjvxsw42qwk FOREIGN KEY (carecall_record_id) REFERENCES CareCallRecord (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE MedicationTakenRecord (
                                       carecall_record_id INT NOT NULL,
                                       id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                       medication_id INT NOT NULL,
                                       medication_schedule_id INT DEFAULT NULL,
                                       taken_status TINYINT DEFAULT NULL,
                                       recorded_at DATETIME(6) NOT NULL,
                                       response_summary VARCHAR(500) DEFAULT NULL,
                                       KEY FKjgswjk7jgi31ksf4a02dt8bjc (carecall_record_id),
                                       KEY FKbvl9qu6xd8qovnpfxgofokvwx (medication_id),
                                       KEY FKfnfuy2mhtnv872da46owxnbf0 (medication_schedule_id),
                                       CONSTRAINT FKbvl9qu6xd8qovnpfxgofokvwx FOREIGN KEY (medication_id) REFERENCES Medication (id),
                                       CONSTRAINT FKfnfuy2mhtnv872da46owxnbf0 FOREIGN KEY (medication_schedule_id) REFERENCES MedicationSchedule (id),
                                       CONSTRAINT FKjgswjk7jgi31ksf4a02dt8bjc FOREIGN KEY (carecall_record_id) REFERENCES CareCallRecord (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE WeeklyStats (
                             absent_count INT NOT NULL,
                             avg_sleep_hours TIME(6) NOT NULL,
                             elder_id INT NOT NULL,
                             id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                             week_end DATE NOT NULL,
                             week_start DATE NOT NULL,
                             blood_sugar_stats JSON DEFAULT NULL,
                             health_summary TEXT DEFAULT NULL,
                             meal_rate JSON NOT NULL,
                             medication_rate JSON NOT NULL,
                             psych_summary JSON DEFAULT NULL,
                             KEY FKlw365wllj7kap43pqgfhaqrib (elder_id),
                             CONSTRAINT FKlw365wllj7kap43pqgfhaqrib FOREIGN KEY (elder_id) REFERENCES Elder (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci; 