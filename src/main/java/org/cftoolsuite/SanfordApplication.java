package org.cftoolsuite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SanfordApplication {

	public static void main(String[] args) {
		SpringApplication.run(SanfordApplication.class, args);
	}

}
