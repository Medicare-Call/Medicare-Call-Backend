-- MealRecord.meal_type를 ENUM 문자열로 전환
ALTER TABLE `MealRecord`
    ADD COLUMN `meal_type_new` ENUM('BREAKFAST', 'LUNCH', 'DINNER') NULL;

UPDATE `MealRecord`
SET `meal_type_new` = CASE `meal_type`
                          WHEN 1 THEN 'BREAKFAST'
                          WHEN 2 THEN 'LUNCH'
                          WHEN 3 THEN 'DINNER'
                          ELSE NULL
    END;

SELECT COUNT(*) AS invalid_meal_type_rows
FROM `MealRecord`
WHERE `meal_type_new` IS NULL AND `meal_type` IS NOT NULL;

-- MealRecord.eaten_status를 ENUM 문자열로 전환
ALTER TABLE `MealRecord`
    ADD COLUMN `eaten_status_new` ENUM('EATEN', 'NOT_EATEN') NULL;

UPDATE `MealRecord`
SET `eaten_status_new` = CASE `eaten_status`
                             WHEN 1 THEN 'EATEN'
                             WHEN 0 THEN 'NOT_EATEN'
                             ELSE NULL
    END;

SELECT COUNT(*) AS invalid_eaten_status_rows
FROM `MealRecord`
WHERE `eaten_status_new` IS NULL AND `eaten_status` IS NOT NULL;

-- 기존 컬럼 제거 후 새 ENUM 컬럼을 본 이름으로 변경
ALTER TABLE `MealRecord`
    DROP COLUMN `meal_type`,
    DROP COLUMN `eaten_status`,
    CHANGE COLUMN `meal_type_new` `meal_type` ENUM('BREAKFAST', 'LUNCH', 'DINNER') NULL,
    CHANGE COLUMN `eaten_status_new` `eaten_status` ENUM('EATEN', 'NOT_EATEN') NULL;
