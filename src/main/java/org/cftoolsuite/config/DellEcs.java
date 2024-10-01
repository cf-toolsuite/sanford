package org.cftoolsuite.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.emc.object.s3.*;
import com.emc.object.s3.jersey.S3JerseyClient;

@Profile("dell-ecs")
@Configuration
public class DellEcs {

    @Bean
    public S3Client s3Client(
            @Value("${ecs.endpoint.host}") String host,
            @Value("${ecs.endpoint.port}") int port,
            @Value("${ecs.accessKey}") String accessKey,
            @Value("${ecs.secretKey}") String secretKey) throws URISyntaxException {
        URI endpoint = port != 443 ? new URI("http://%s:%d".formatted(host, port)) : new URI("https://%s".formatted(host));
        S3Config config = new S3Config(endpoint);
        config.withIdentity(accessKey).withSecretKey(secretKey);
        S3Client s3Client = new S3JerseyClient(config);
        return s3Client;
    }
}
