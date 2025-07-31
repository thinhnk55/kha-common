package com.defi.common.util.minio;

import com.defi.common.util.slugify.SlugifyUtil;
import com.defi.common.util.string.RandomStringUtil;
import io.minio.*;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service class for MinIO object storage operations.
 * 
 * <p>
 * This service provides methods for managing files in MinIO object storage,
 * including upload, download, delete operations, and automatic bucket creation.
 * It also provides utilities for generating unique object keys with timestamp
 * and UUID patterns.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * MinioService minioService = new MinioService();
 * 
 * // Upload a file
 * ObjectWriteResponse response = minioService.uploadFile(
 *         "my-bucket", "path/to/file.jpg", inputStream, fileSize, "image/jpeg");
 * 
 * // Download a file
 * InputStream fileData = minioService.downloadFile("my-bucket", "path/to/file.jpg");
 * 
 * // Generate unique object key
 * String objectKey = minioService.generateObjectKey("my-document.pdf");
 * }</pre>
 * 
 * @author Defi Team
 * @since 1.0.5
 */
public class MinioService {

    /**
     * Default constructor for MinioService.
     */
    public MinioService() {
        // Default constructor
    }

    /**
     * Uploads a file to MinIO storage.
     * 
     * @param bucketName    the name of the bucket to upload to
     * @param objectKey     the key/path for the object in storage
     * @param inputStream   the input stream containing file data
     * @param contentLength the size of the file in bytes
     * @param contentType   the MIME type of the file
     * @return ObjectWriteResponse containing upload metadata
     * @throws Exception if upload fails or bucket operations fail
     */
    public ObjectWriteResponse uploadFile(String bucketName, String objectKey, InputStream inputStream,
            long contentLength, String contentType) throws Exception {
        ensureBucketExists(bucketName);
        return MinioClientProvider.getInstance().putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .stream(inputStream, contentLength, -1)
                        .contentType(contentType)
                        .build());
    }

    /**
     * Downloads a file from MinIO storage.
     * 
     * @param bucketName the name of the bucket containing the file
     * @param objectKey  the key/path of the object to download
     * @return InputStream containing the file data
     * @throws Exception if download fails or object doesn't exist
     */
    public InputStream downloadFile(String bucketName, String objectKey) throws Exception {
        return MinioClientProvider.getInstance().getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .build());
    }

    /**
     * Deletes a file from MinIO storage.
     * 
     * @param bucketName the name of the bucket containing the file
     * @param objectKey  the key/path of the object to delete
     * @throws Exception if delete operation fails
     */
    public void deleteFile(String bucketName, String objectKey) throws Exception {
        MinioClientProvider.getInstance().removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .build());
    }

    /**
     * Checks if a file exists in MinIO storage.
     * 
     * @param bucketName the name of the bucket to check
     * @param objectKey  the key/path of the object to check
     * @return true if file exists, false otherwise
     */
    public boolean fileExists(String bucketName, String objectKey) {
        try {
            MinioClientProvider.getInstance().statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets metadata information about a file in MinIO storage.
     * 
     * @param bucketName the name of the bucket containing the file
     * @param objectKey  the key/path of the object to get info for
     * @return StatObjectResponse containing file metadata
     * @throws Exception if object doesn't exist or operation fails
     */
    public StatObjectResponse getFileInfo(String bucketName, String objectKey) throws Exception {
        return MinioClientProvider.getInstance().statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .build());
    }

    /**
     * Creates a bucket if it doesn't already exist.
     * 
     * @param bucketName the name of the bucket to create
     * @return true if bucket was created, false if it already existed
     * @throws Exception if bucket creation fails
     */
    public boolean createBucketIfNotExists(String bucketName) throws Exception {
        boolean exists = MinioClientProvider.getInstance().bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build());

        if (!exists) {
            MinioClientProvider.getInstance().makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build());
            return true;
        }
        return false;
    }

    private void ensureBucketExists(String bucketName) throws Exception {
        createBucketIfNotExists(bucketName);
    }

    /**
     * Generates a unique object key for storage based on timestamp, UUID, and
     * slugified filename.
     * 
     * <p>
     * The generated key follows the pattern:
     * {year}/{month}/{day}/{hour}/{uuid}/{slugified-filename}.{extension}
     * This provides automatic organization by time and ensures uniqueness.
     * </p>
     * 
     * @param fileName the original filename to generate key from
     * @return unique object key suitable for MinIO storage
     */
    public String generateObjectKey(String fileName) {
        String uuid = RandomStringUtil.uuidV7().toString();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        String slugifiedFileName = SlugifyUtil.slugify(fileName);
        String timePattern = "yyyy/MM/dd/HH";
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern(timePattern));
        return String.format("%s/%s/%s.%s", time, uuid, slugifiedFileName, extension);
    }
}