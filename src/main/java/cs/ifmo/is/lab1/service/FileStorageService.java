package cs.ifmo.is.lab1.service;

import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.InputStream;

@ApplicationScoped
public class FileStorageService {
    private MinioClient minioClient;
    private static final String BUCKET_NAME = "import-files";

    @PostConstruct
    public void init() {
        minioClient = MinioClient.builder()
                .endpoint("http://localhost:9000") // Ваш URL MinIO
                .credentials("minioadmin", "minioadmin") // Ваши креды MinIO
                .build();
        ensureBucketExists();
    }

    private void ensureBucketExists() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при инициализации MinIO: " + e.getMessage(), e);
        }
    }

    public void uploadFile(String fileName, InputStream fileStream, long size, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(fileName)
                    .stream(fileStream, size, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке файла в MinIO: " + e.getMessage(), e);
        }
    }

    public InputStream downloadFile(String fileName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(BUCKET_NAME).object(fileName).build());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при скачивании файла из MinIO: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(BUCKET_NAME).object(fileName).build());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при удалении файла из MinIO: " + e.getMessage(), e);
        }
    }

}
