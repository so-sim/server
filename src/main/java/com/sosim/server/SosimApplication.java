package com.sosim.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SosimApplication {

	public static void main(String[] args) {
		SpringApplication.run(SosimApplication.class, args);
	}

}
