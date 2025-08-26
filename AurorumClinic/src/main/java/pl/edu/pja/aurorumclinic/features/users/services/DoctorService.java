package pl.edu.pja.aurorumclinic.features.users.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pja.aurorumclinic.features.users.dtos.GetDoctorResponse;

import java.io.IOException;
import java.util.List;

@Service
public interface DoctorService {
    List<GetDoctorResponse> getAllDoctors() throws IOException;
    void uploadProfilePicture(MultipartFile image, Long doctorId) throws IOException;
}
