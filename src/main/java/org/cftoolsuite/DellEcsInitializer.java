package org.cftoolsuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.emc.object.s3.S3Client;

@Profile("dell-ecs")
@Component
public class DellEcsInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(DellEcsInitializer.class);

    private final S3Client s3Client;
    private final String bucketName;

    public DellEcsInitializer(S3Client s3Client, @Value("${ecs.bucket.name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void onApplicationEvent(ApplicationReadyEvent event) {
        s3Client.listBuckets();
        log.info("Successfully connected to Dell ECS server");
        log.info("Checking if bucket {} already exists", bucketName);
        if (!s3Client.bucketExists(bucketName)) {
            s3Client.createBucket(bucketName);
            log.info("Bucket created successfully: {}", bucketName);
        } else {
            log.info("Bucket already exists: {}", bucketName);
        }
    }

    @Recover
    public void recover(Exception e) {
        log.error("All retry attempts failed. Dell ECS initialization unsuccessful!", e);
    }
}