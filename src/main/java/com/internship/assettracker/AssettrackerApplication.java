package com.internship.assettracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.internship.assettracker", "com.i.a"})
@EntityScan("com.i.a.entity")
@EnableJpaRepositories("com.i.a.repository")
public class AssettrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssettrackerApplication.class, args);
	}

}
