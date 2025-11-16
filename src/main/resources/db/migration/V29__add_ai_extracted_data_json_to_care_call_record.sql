ALTER TABLE CareCallRecord
ADD COLUMN ai_extracted_data_json TEXT COMMENT 'AI로부터 추출된 건강 데이터 전체 JSON';
