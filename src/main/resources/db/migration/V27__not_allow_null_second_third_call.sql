ALTER TABLE CareCallSetting
    MODIFY COLUMN second_call_time TIME NOT NULL;

ALTER TABLE CareCallSetting
    MODIFY COLUMN third_call_time TIME NOT NULL;