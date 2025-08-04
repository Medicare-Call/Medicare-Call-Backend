-- 기획 변경에 따라 3차 케어콜 시간을 관리할 third_call_time 추가
ALTER TABLE CareCallSetting
ADD COLUMN third_call_time TIME;