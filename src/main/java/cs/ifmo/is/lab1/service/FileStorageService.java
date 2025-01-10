package cs.ifmo.is.lab1.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

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

    private static final ConcurrentHashMap<String, LocalDateTime> uploadedFilesCache = new ConcurrentHashMap<>();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public String uploadFile(String fileName, InputStream fileStream, long size) throws ConnectException {
        try {
            String currentTime = LocalTime.now().format(TIME_FORMATTER);
            String newFileName = fileName.substring(0, fileName.lastIndexOf("."))
                    + "_" + currentTime + fileName.substring(fileName.lastIndexOf("."));

            LocalDateTime now = LocalDateTime.now();

            if (uploadedFilesCache.containsKey(fileName)) {
                LocalDateTime lastUploadedTime = uploadedFilesCache.get(fileName);
                if (Math.abs(java.time.Duration.between(lastUploadedTime, now).getSeconds()) <= 1) {
                    LOGGER.info("Skipping file upload due to minimal time difference: " + fileName);
                    return null;
                }
            }

            String contentType = "application/json";
            LOGGER.info("Uploading JSON file to MinIO: " + newFileName + " (size: " + size + ", contentType: " + contentType + ")");

            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(newFileName)
                    .contentType(contentType);

            if (size > 0) {
                builder.stream(fileStream, size, -1);
            } else {
                builder.stream(fileStream, -1, 5 * 1024 * 1024);
            }

            minioClient.putObject(builder.build());

            uploadedFilesCache.put(fileName, now);
            LOGGER.info("JSON file successfully uploaded: " + newFileName);
            return newFileName;
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

    public boolean fileExists(String fileName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(fileName)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            throw new RuntimeException("Ошибка проверки наличия файла: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка проверки наличия файла: " + e.getMessage(), e);
        }
    }


    public String generateDownloadUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(BUCKET_NAME)
                            .object(fileName)
                            .expiry(7, TimeUnit.DAYS)
                            .extraQueryParams(Map.of("response-content-disposition", "attachment; filename=\"" + fileName + "\""))
                            .build());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating download URL", e);
            throw new RuntimeException("Ошибка при генерации URL для скачивания: " + e.getMessage(), e);
        }
    }


}
