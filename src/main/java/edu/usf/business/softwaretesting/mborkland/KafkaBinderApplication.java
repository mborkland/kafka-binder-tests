package edu.usf.business.softwaretesting.mborkland;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;

@SpringBootApplication(exclude = GsonAutoConfiguration.class)
public class KafkaBinderApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaBinderApplication.class, args);
	}
}
