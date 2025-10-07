package pl.edu.pja.aurorumclinic.shared.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ObjectStorageServiceImpl implements ObjectStorageService {

    private final S3Client s3Client;

    @Value("${cloud.aws.bucket.name}")
    private String s3BucketName;


    @Override
    public String uploadObject(MultipartFile file) throws IOException {
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(s3BucketName)
                        .key(file.getOriginalFilename())
                        .build(),
                RequestBody.fromBytes(file.getBytes()));
        return file.getOriginalFilename();
    }

    @Override
    public String generateUrl(String fileName) {
        if (fileName == null) {
            GetUrlRequest objectRequest = GetUrlRequest.builder()
                    .bucket(s3BucketName)
                    .key("img.png")
                    .build();
            return String.valueOf(s3Client.utilities().getUrl(objectRequest));
        } else {
            GetUrlRequest objectRequest = GetUrlRequest.builder()
                    .bucket(s3BucketName)
                    .key(fileName)
                    .build();
            return String.valueOf(s3Client.utilities().getUrl(objectRequest));
        }
    }
}
