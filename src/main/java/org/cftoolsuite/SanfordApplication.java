package org.cftoolsuite;

import java.util.Set;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SanfordApplication {

	private static final String DEFAULT_STORAGE_PROVIDER = "minio";
	private static final Set<String> SUPPORTED_STORAGE_PROVIDERS = Set.of("minio");
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SanfordApplication.class);
        activateAdditionalProfiles(app);
        app.run(args);
    }

	private static void activateAdditionalProfiles(SpringApplication app) {
		String storageProvider = System.getProperty("storage.provider");

		if (storageProvider == null) {
            storageProvider = System.getenv("STORAGE_PROVIDER");
        }

		if (storageProvider == null || !SUPPORTED_STORAGE_PROVIDERS.contains(storageProvider) ) {
			System.setProperty("storage.provider", DEFAULT_STORAGE_PROVIDER);
			storageProvider = DEFAULT_STORAGE_PROVIDER;
		}

        if ("minio".equalsIgnoreCase(storageProvider)) {
            app.setAdditionalProfiles("minio");
        }

	}
}
