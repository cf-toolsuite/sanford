package org.cftoolsuite.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.io.FilenameUtils;
import org.cftoolsuite.domain.FileMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Item;

@Service
public class FileService {

    private final MinioClient minioClient;
    private final String bucketName;

    public FileService(MinioClient minioClient, @Value("${minio.bucket.name}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    public FileMetadata uploadFile(MultipartFile file) {
        try {
            String objectId = UUID.randomUUID().toString();
            String fileName = file.getOriginalFilename();
            String fileExtension = FilenameUtils.getExtension(fileName);
            String mimeType = file.getContentType();

            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .userMetadata(Map.of("oid", objectId, "mimeType", mimeType))
                    .contentType(mimeType)
                    .build()
            );

            return new FileMetadata(objectId, fileName, fileExtension, mimeType);
        } catch (ErrorResponseException | InvalidKeyException | InvalidResponseException | InsufficientDataException |
            InternalException | NoSuchAlgorithmException | IOException | ServerException | XmlParserException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public FileMetadata getFileMetadata(String fileName) {
        try {
            StatObjectResponse item = minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build()
            );

            String objectId = item.userMetadata().get("oid");
            String mimeType = item.userMetadata().get("mimeType");
            return new FileMetadata(
                objectId,
                fileName,
                FilenameUtils.getExtension(fileName),
                mimeType
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
            InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            throw new RuntimeException("Failed to retrieve file metadata", e);
        }
    }

    public List<FileMetadata> getAllFileMetadata() {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).build()
            );

            List<FileMetadata> allFileMetadata = StreamSupport.stream(results.spliterator(), false)
                .map(itemResult -> {
                    try {
                        Item item = itemResult.get();
                        String fileName = item.objectName();
                        String objectId = item.userMetadata().get("oid");
                        String mimeType = item.userMetadata().get("mimeType");
                        return new FileMetadata(
                            objectId,
                            fileName,
                            FilenameUtils.getExtension(fileName),
                            mimeType
                        );
                    } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                        InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
                        throw new RuntimeException("Error retrieving file metadata", e);
                    }
                })
                .collect(Collectors.toList());

            return allFileMetadata;
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to retrieve all file metadata", e);
        }
    }

    public Resource downloadFile(String fileName) {
        try {
            InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build()
            );

            return new InputStreamResource(stream);
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
            InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            throw new RuntimeException("Failed to download file", e);
        }
    }
}
