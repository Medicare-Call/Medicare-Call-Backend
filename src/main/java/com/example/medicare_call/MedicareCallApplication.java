package com.example.medicare_call;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MedicareCallApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedicareCallApplication.class, args);
	}

}


