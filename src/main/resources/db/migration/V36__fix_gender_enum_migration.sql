-- gender_new가 존재하고 gender가 없는 상태 마무리

-- 1. 컬럼 rename + NOT NULL 적용
ALTER TABLE `Elder`
    CHANGE COLUMN `gender_new` `gender` ENUM('MALE','FEMALE') NOT NULL;