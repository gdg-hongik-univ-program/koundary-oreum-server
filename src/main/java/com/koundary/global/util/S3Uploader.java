package com.koundary.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final Set<String> ALLOWED = Set.of("image/jpeg","image/png","image/webp");

    public String upload(MultipartFile file, String dirName) {
        if (file.isEmpty()) throw new IllegalArgumentException("빈 파일입니다.");
        if (!ALLOWED.contains(file.getContentType())) {
            throw new IllegalArgumentException("허용되지 않은 Content-Type: " + file.getContentType());
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new IllegalArgumentException("파일 이름이 없습니다.");
        }
        String ext = getExtension(originalName);

        String key = dirName.replaceAll("/+$","") + "/" + UUID.randomUUID() + ext;

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                // 버킷 정책에서 SSE 강제했다면 아래 한 줄 필요
                .serverSideEncryption("AES256")
                .build();

        try {
            s3.putObject(req, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }

        // 비공개 버킷이므로 URL 대신 key 반환(권장)
        return key;
    }

    public void delete(String key) {
        s3.deleteObject(b -> b.bucket(bucket).key(key));
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot).toLowerCase() : "";
    }
}
