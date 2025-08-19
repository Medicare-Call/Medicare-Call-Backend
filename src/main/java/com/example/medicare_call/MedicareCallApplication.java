package com.example.medicare_call;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class MedicareCallApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedicareCallApplication.class, args);
	}

}


