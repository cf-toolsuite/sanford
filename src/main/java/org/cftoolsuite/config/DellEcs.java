package org.cftoolsuite.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.cftoolsuite.DellEcsInitializer;
import org.cftoolsuite.domain.AppProperties;
import org.cftoolsuite.service.DellEcsFileService;
import org.cftoolsuite.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.emc.object.s3.S3Client;
import com.emc.object.s3.S3Config;
import com.emc.object.s3.jersey.S3JerseyClient;

@Configuration
@ConditionalOnProperty(name = "storage.provider", havingValue = "dell-ecs")
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

    @Bean
    public DellEcsInitializer dellEcsInitializer(S3Client s3Client, @Value("${ecs.bucket.name}") String bucketName) {
        return new DellEcsInitializer(s3Client, bucketName);
    }

    @Bean
    public FileService dellEcsFileService(S3Client s3Client, @Value("${ecs.bucket.name}") String bucketName, AppProperties appProperties) {
        return new DellEcsFileService(s3Client, bucketName, appProperties);
    }
}
