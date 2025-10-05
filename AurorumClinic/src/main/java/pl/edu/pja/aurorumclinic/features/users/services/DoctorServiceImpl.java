package pl.edu.pja.aurorumclinic.features.users.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pja.aurorumclinic.features.users.dtos.response.RecommendedDoctorResponse;
import pl.edu.pja.aurorumclinic.features.users.dtos.response.SearchDoctorResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final ObjectStorageService objectStorageService;

    @Override
    public Page<SearchDoctorResponse> searchAllDoctors(String query, int page, int size) throws IOException {
        Pageable pageable = PageRequest.of(page, size);
        Page<SearchDoctorResponse> doctorsFromDb = doctorRepository.findAllByQuery(query, pageable);
        return doctorsFromDb;
    }

    @Transactional
    @Override
    public void uploadProfilePicture(MultipartFile image, Long doctorId) throws IOException {
        Doctor doctorFromDb = doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        String imagePath = objectStorageService.uploadObject(image);
        doctorFromDb.setProfilePicture(imagePath);
    }

    @Override
    public List<LocalDateTime> getAppointmentSlots(Long doctorId, LocalDateTime startedAt, LocalDateTime finishedAt, Integer serviceDuration) {
        doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        return doctorRepository.appointmentSlots(startedAt, finishedAt, serviceDuration, Math.toIntExact(doctorId))
                .stream()
                .map(Timestamp::toLocalDateTime)
                .toList();
    }

    @Override
    public Page<RecommendedDoctorResponse> getRecommendedDoctors(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RecommendedDoctorResponse> doctorsFromDb = doctorRepository.findAllRecommendedDtos(pageable);
        return doctorsFromDb;
    }

}
