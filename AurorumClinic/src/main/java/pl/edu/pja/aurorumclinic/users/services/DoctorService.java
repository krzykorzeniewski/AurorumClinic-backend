package pl.edu.pja.aurorumclinic.users.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pja.aurorumclinic.models.Doctor;
import pl.edu.pja.aurorumclinic.users.dtos.GetDoctorResponseDto;

import java.io.IOException;
import java.util.List;

@Service
public interface DoctorService {

    Doctor uploadProfilePicture(Long id, MultipartFile picture) throws IOException;

    List<GetDoctorResponseDto> getAllDoctors();
}
