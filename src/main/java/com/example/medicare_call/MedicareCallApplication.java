package com.example.medicare_call;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class MedicareCallApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedicareCallApplication.class, args);
	}

}


@RestController
class HomeController {

	@GetMapping("/")
	public String home() {
		return "Hello, Medicare-Call!";
	}
}