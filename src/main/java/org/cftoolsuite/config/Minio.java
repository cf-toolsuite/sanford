package org.cftoolsuite.config;

import io.minio.MinioClient;
import org.cftoolsuite.MinioInitializer;
import org.cftoolsuite.domain.AppProperties;
import org.cftoolsuite.service.FileService;
import org.cftoolsuite.service.MinioFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class Minio {

    private static Logger log = LoggerFactory.getLogger(Minio.class);

    // @see https://min.io/docs/minio/linux/developers/java/API.html#id1
    @Bean
    public MinioClient minioClient(
            @Value("${minio.endpoint.host}") String host,
            @Value("${minio.endpoint.port}") int port,
            @Value("${minio.endpoint.scheme}") String scheme,
            @Value("${minio.accessKey}") String accessKey,
            @Value("${minio.secretKey}") String secretKey) {
        MinioClient.Builder mcb = MinioClient.builder();
        log.trace("MinIO client configured with [ host: {}, port: {}, scheme: {}, accessKey: {}, secretKey: {} ]", host, port, scheme, accessKey, secretKey);
        return mcb
                .endpoint(host, port, scheme.equalsIgnoreCase("https"))
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
