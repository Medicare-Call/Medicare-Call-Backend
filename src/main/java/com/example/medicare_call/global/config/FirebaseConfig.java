package com.example.medicare_call.global.config;

import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials:#{null}}")
    private String credentialsJson;

    @PostConstruct
    public void init() {

        if (credentialsJson == null || credentialsJson.trim().isEmpty()) {
            log.warn("Firebase credentials이 설정되지 않아 초기화를 건너뜁니다. (환경 변수 'FIREBASE_CREDENTIALS' 필요)");
            return;
        }

        try (InputStream serviceAccount = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))) {

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // FirebaseApp 중복 초기화 방지
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("FirebaseApp이 환경 변수 credentials을 사용하여 성공적으로 초기화되었습니다.");
            }

        }  catch (IOException e) {
            throw new CustomException(ErrorCode.FIREBASE_INITIALIZATION_FAILED);
        }

    }
}
