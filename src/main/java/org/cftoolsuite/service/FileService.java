package org.cftoolsuite.service;

import org.cftoolsuite.domain.FileMetadata;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;


public interface FileService {

    public FileMetadata uploadFile(Path filePath);

    public FileMetadata uploadFile(MultipartFile file);

    public FileMetadata getFileMetadata(String fileName);

    public List<FileMetadata> getAllFileMetadata();

    public Resource downloadFile(String fileName);

    public void deleteFile(String fileName);
}
