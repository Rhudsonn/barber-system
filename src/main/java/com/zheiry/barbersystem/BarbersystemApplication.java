package com.zheiry.barbersystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BarbersystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(BarbersystemApplication.class, args);
	}

}
