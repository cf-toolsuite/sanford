package org.cftoolsuite.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.minio.MinioClient;

@Profile("minio")
@Configuration
public class Minio {

    // @see https://min.io/docs/minio/linux/developers/java/API.html#id1
    @Bean
    public MinioClient minioClient(
            @Value("${minio.endpoint.host}") String host,
            @Value("${minio.endpoint.port}") int port,
            @Value("${minio.accessKey}") String accessKey,
            @Value("${minio.secretKey}") String secretKey) {
        MinioClient.Builder mcb = MinioClient.builder();
        if (port != 443) {
            mcb.endpoint(host, port, false);
        } else {
            mcb.endpoint(host, 443, true);
        }
        return mcb
                .credentials(accessKey, secretKey)
                .build();
    }
}
