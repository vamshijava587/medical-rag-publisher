package com.vamshi.rag;

import com.vamshi.rag.common.MedicalAiRagProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MedicalAiRagProperties.class)
public class MedicalRagPublisherApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedicalRagPublisherApplication.class, args);
	}

}
