package org.cftoolsuite.config;

import org.cftoolsuite.MinioInitializer;
import org.cftoolsuite.domain.AppProperties;
import org.cftoolsuite.service.FileService;
import org.cftoolsuite.service.MinioFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;


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

    @Bean
    public MinioInitializer minioInitializer(MinioClient minioClient, @Value("${minio.bucket.name}") String bucketName) {
        return new MinioInitializer(minioClient, bucketName);
    }

    @Bean
    public FileService minioFileService(MinioClient minioClient, @Value("${minio.bucket.name}") String bucketName, AppProperties appProperties) {
        return new MinioFileService(minioClient, bucketName, appProperties);
    }
}
