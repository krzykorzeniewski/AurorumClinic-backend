package pl.edu.pja.aurorumclinic.users.services;

import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.users.dtos.GetDoctorResponse;

import java.util.List;

@Service
public interface DoctorService {
    List<GetDoctorResponse> getAllDoctors();
}
