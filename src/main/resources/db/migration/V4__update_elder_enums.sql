-- ElderRelation enum 업데이트 (SPOUSE 제거, 새로운 값들 추가)
ALTER TABLE Elder MODIFY COLUMN relationship ENUM('CHILD','GRANDCHILD','SIBLING','RELATIVE','ACQUAINTANCE') NOT NULL;

-- ResidenceType enum 업데이트 (WITH_SPOUSE, WITH_ME 제거)
ALTER TABLE Elder MODIFY COLUMN residence_type ENUM('ALONE','WITH_FAMILY') NOT NULL; 