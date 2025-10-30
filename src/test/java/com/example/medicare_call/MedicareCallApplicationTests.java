package com.example.medicare_call;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
class MedicareCallApplicationTests {

	@MockBean
	private FirebaseApp firebaseApp;

	@MockBean
	private FirebaseMessaging firebaseMessaging;

	@Test
	void contextLoads() {
	}

}
