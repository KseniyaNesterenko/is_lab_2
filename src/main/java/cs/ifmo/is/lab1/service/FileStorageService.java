package cs.ifmo.is.lab1.service;

import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.InputStream;
import java.net.ConnectException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class FileStorageService {
    private static final Logger LOGGER = Logger.getLogger(FileStorageService.class.getName());
    private MinioClient minioClient;
    private static final String BUCKET_NAME = "import-files";

    @PostConstruct
    public void init() {
        try {
            LOGGER.info("Initializing MinIO client...");
            minioClient = MinioClient.builder()
                    .endpoint("http://127.0.0.1:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

            LOGGER.info("Ensuring bucket exists: " + BUCKET_NAME);
            ensureBucketExists();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing MinIO client", e);
            throw new RuntimeException("Ошибка при настройке MinIO: " + e.getMessage(), e);
        }
    }

    private void ensureBucketExists() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build());
            if (!found) {
                LOGGER.info("Bucket not found. Creating bucket: " + BUCKET_NAME);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
            } else {
                LOGGER.info("Bucket already exists: " + BUCKET_NAME);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error ensuring bucket exists", e);
            throw new RuntimeException("Ошибка при проверке/создании бакета MinIO: " + e.getMessage(), e);
        }
    }

    public String uploadFile(String fileName, InputStream fileStream, long size) throws ConnectException {
        try {
            String contentType = "application/json";
            LOGGER.info("Uploading JSON file to MinIO: " + fileName + " (size: " + size + ", contentType: " + contentType + ")");

            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(fileName)
                    .contentType(contentType);

            if (size > 0) {
                builder.stream(fileStream, size, -1);
            } else {
                builder.stream(fileStream, -1, 5 * 1024 * 1024);
            }

            minioClient.putObject(builder.build());

            LOGGER.info("JSON file successfully uploaded: " + fileName);
            return fileName;
        } catch (ErrorResponseException e) {
            LOGGER.log(Level.SEVERE, "MinIO error during file upload", e);
            throw new RuntimeException("Ошибка MinIO: " + e.errorResponse().message(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error uploading JSON file to MinIO", e);
            throw new RuntimeException("Ошибка при загрузке JSON файла в MinIO: " + e.getMessage(), e);
        }
    }

    public InputStream downloadFile(String fileName) {
        try {
            LOGGER.info("Downloading file from MinIO: " + fileName);
            InputStream fileStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(fileName)
                    .build());
            LOGGER.info("File successfully downloaded: " + fileName);
            return fileStream;
        } catch (ErrorResponseException e) {
            LOGGER.log(Level.SEVERE, "MinIO error during file download", e);
            throw new RuntimeException("Ошибка MinIO при скачивании файла: " + e.errorResponse().message(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error downloading file from MinIO", e);
            throw new RuntimeException("Ошибка при скачивании файла из MinIO: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            LOGGER.info("Deleting file from MinIO: " + fileName);
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(fileName)
                    .build());
            LOGGER.info("File successfully deleted: " + fileName);
        } catch (ErrorResponseException e) {
            LOGGER.log(Level.SEVERE, "MinIO error during file deletion", e);
            throw new RuntimeException("Ошибка MinIO при удалении файла: " + e.errorResponse().message(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting file from MinIO", e);
            throw new RuntimeException("Ошибка при удалении файла из MinIO: " + e.getMessage(), e);
        }
    }
}
