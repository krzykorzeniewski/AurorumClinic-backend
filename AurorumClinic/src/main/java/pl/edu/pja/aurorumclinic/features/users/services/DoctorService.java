package pl.edu.pja.aurorumclinic.features.users.services;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pja.aurorumclinic.features.users.dtos.response.RecommendedDoctorResponse;
import pl.edu.pja.aurorumclinic.features.users.dtos.response.SearchDoctorResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public interface DoctorService {

    Page<SearchDoctorResponse> searchAllDoctors(String query, int page, int size) throws IOException;
    void uploadProfilePicture(MultipartFile image, Long doctorId) throws IOException;

    List<LocalDateTime> getAppointmentSlots(Long doctorId, LocalDateTime startedAt,
                                            LocalDateTime finishedAt, Integer serviceDuration);

    Page<RecommendedDoctorResponse> getRecommendedDoctors(int page, int size);
}
