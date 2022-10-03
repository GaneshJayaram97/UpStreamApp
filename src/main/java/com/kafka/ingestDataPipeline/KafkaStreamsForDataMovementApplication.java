package com.kafka.ingestDataPipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaStreamsForDataMovementApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsForDataMovementApplication.class, args);
	}

}
