package com.example.medicare_call.global.config;

import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.file-path}")
    String filePath;

    @Bean
    FirebaseApp firebaseApp() throws IOException {

        if (filePath== null || filePath.trim().isEmpty()) {
            log.warn("Firebase 설정되지 않아 초기화를 건너뜁니다. (환경 변수 'FIlE_PATH' 필요)");
            return FirebaseApp.getInstance();
        }

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(filePath).getInputStream());

        FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(googleCredentials)
                .build();

        //중복 초기화 방지
        if (FirebaseApp.getApps().isEmpty()) {
            log.info("FirebaseApp이 환경 변수 credentials을 사용하여 성공적으로 초기화되었습니다.");
            return FirebaseApp.initializeApp(firebaseOptions);
        }

        return FirebaseApp.getInstance();
    }

    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

}
