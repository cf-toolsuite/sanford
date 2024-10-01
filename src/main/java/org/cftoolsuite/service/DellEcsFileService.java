package org.cftoolsuite.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.cftoolsuite.domain.AppProperties;
import org.cftoolsuite.domain.FileMetadata;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.emc.object.s3.S3Client;
import com.emc.object.s3.S3ObjectMetadata;
import com.emc.object.s3.bean.GetObjectResult;
import com.emc.object.s3.bean.ListObjectsResult;
import com.emc.object.s3.bean.S3Object;
import com.emc.object.s3.request.PutObjectRequest;

public class DellEcsFileService implements FileService {

    private final S3Client s3Client;
    private final String bucketName;
    private final Map<String, String> supportedContentTypes;

    public DellEcsFileService(S3Client s3Client, String bucketName, AppProperties appProperties) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.supportedContentTypes = appProperties.supportedContentTypes();
    }

    public FileMetadata uploadFile(MultipartFile file) {
        try {
            String objectId = UUID.randomUUID().toString();
            String fileName = file.getOriginalFilename();
            String fileExtension = FilenameUtils.getExtension(fileName);
            String contentType = file.getContentType() == null ? supportedContentTypes.get(fileExtension) : file.getContentType();

            InputStream inputStream = file.getInputStream();
            String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            S3ObjectMetadata metadata = new S3ObjectMetadata().withContentType(contentType);
            metadata.addUserMetadata("oid", objectId);
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, content);

            s3Client.putObject(request.withObjectMetadata(metadata));

            return new FileMetadata(objectId, fileName, fileExtension, contentType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public FileMetadata getFileMetadata(String fileName) {
        S3ObjectMetadata metadata = s3Client.getObjectMetadata(bucketName, fileName);
        Map<String, String> userMetadata = metadata.getUserMetadata();

        String objectId = userMetadata.get("oid");
        String contentType = metadata.getContentType();
        return new FileMetadata(
            objectId,
            fileName,
            FilenameUtils.getExtension(fileName),
            contentType
        );
    }

    public List<FileMetadata> getAllFileMetadata() {
        ListObjectsResult result = s3Client.listObjects(bucketName);
        List<FileMetadata> allFileMetadata = new ArrayList<>();

        for (S3Object object : result.getObjects()) {
            String fileName = object.getKey();
            S3ObjectMetadata metadata = s3Client.getObjectMetadata(bucketName, fileName);
            Map<String, String> userMetadata = metadata.getUserMetadata();
            String objectId = userMetadata.get("oid");
            String contentType = metadata.getContentType();

            allFileMetadata.add(new FileMetadata(
                objectId,
                fileName,
                FilenameUtils.getExtension(fileName),
                contentType
            ));
        }

        return allFileMetadata;
    }

    public Resource downloadFile(String fileName) {
        GetObjectResult<InputStream> result = s3Client.getObject(bucketName, fileName);
        InputStream stream = result.getObject();
        return new InputStreamResource(stream);
    }
}