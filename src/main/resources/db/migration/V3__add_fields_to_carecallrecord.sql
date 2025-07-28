ALTER TABLE CareCallRecord
ADD COLUMN start_time DATETIME,
ADD COLUMN end_time DATETIME,
ADD COLUMN call_status VARCHAR(20),
ADD COLUMN transcription_language VARCHAR(10),
ADD COLUMN transcription_text TEXT; 