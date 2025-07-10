-- BloodSugarRecord 테이블의 value 컬럼을 blood_sugar_value로 변경
ALTER TABLE BloodSugarRecord CHANGE COLUMN `value` `blood_sugar_value` DECIMAL(10,2); 