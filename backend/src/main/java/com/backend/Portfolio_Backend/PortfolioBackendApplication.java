package com.backend.Portfolio_Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class PortfolioBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfolioBackendApplication.class, args);
	}

}
