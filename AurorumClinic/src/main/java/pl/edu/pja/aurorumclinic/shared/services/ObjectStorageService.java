package pl.edu.pja.aurorumclinic.shared.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ObjectStorageService {

    String uploadObject(MultipartFile file) throws IOException;
    String generateSignedUrl(String fileName) throws IOException;

}
