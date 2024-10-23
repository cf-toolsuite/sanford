package org.cftoolsuite.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.io.FilenameUtils;
import org.cftoolsuite.domain.AppProperties;
import org.cftoolsuite.domain.FileMetadata;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
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

public class MinioFileService implements FileService {

    private final MinioClient minioClient;
    private final String bucketName;
    private final Map<String, String> supportedContentTypes;

    public MinioFileService(MinioClient minioClient, String bucketName, AppProperties appProperties) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        this.supportedContentTypes = appProperties.supportedContentTypes();
    }

    @Override
    public FileMetadata uploadFile(Path filePath) {
        String objectId = UUID.randomUUID().toString();
        String fileName = new PathResource(filePath).getFilename();
        String fileExtension = FilenameUtils.getExtension(fileName);
        String contentType = supportedContentTypes.get(fileExtension);
        try(InputStream stream = new ByteArrayInputStream(Files.readAllBytes(filePath))) {
            long objectSize = Files.size(filePath);
            return uploadFile(objectId, fileName, fileExtension, contentType, objectSize, stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to obtain stream from " + fileName, e);
        }
    }

    @Override
    public FileMetadata uploadFile(MultipartFile file) {
        String objectId = UUID.randomUUID().toString();
        String fileName = file.getResource().getFilename();
        String fileExtension = FilenameUtils.getExtension(fileName);
        String contentType = file.getContentType() == null ? supportedContentTypes.get(fileExtension) : file.getContentType();
        long objectSize = file.getSize();
        try(InputStream stream = file.getInputStream()) {
            return uploadFile(objectId, fileName, fileExtension, contentType, objectSize, stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to obtain stream from " + fileName, e);
        }
    }

    protected FileMetadata uploadFile(String objectId, String fileName, String fileExtension, String contentType, long objectSize, InputStream stream) {
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(stream, objectSize, -1)
                    .userMetadata(Map.of("oid", objectId, "contentType", contentType))
                    .contentType(contentType)
                    .build()
            );

            return new FileMetadata(objectId, fileName, fileExtension, contentType);
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
            String contentType = item.contentType();
            return new FileMetadata(
                objectId,
                fileName,
                FilenameUtils.getExtension(fileName),
                contentType
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
            InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            throw new RuntimeException("Failed to retrieve file metadata", e);
        }
    }

    public List<FileMetadata> getAllFileMetadata() {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).includeUserMetadata(true).build()
            );

            List<FileMetadata> allFileMetadata = StreamSupport.stream(results.spliterator(), false)
                .map(itemResult -> {
                    try {
                        Item item = itemResult.get();
                        String fileName = item.objectName();
                        String objectId = item.userMetadata().get(String.format("X-Amz-Meta-%s", "Oid"));
                        String contentType = item.userMetadata().get(String.format("X-Amz-Meta-%s", "Contenttype"));
                        return new FileMetadata(
                            objectId,
                            fileName,
                            FilenameUtils.getExtension(fileName),
                            contentType
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

    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
            InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException | XmlParserException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}
