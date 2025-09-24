package pl.edu.pja.aurorumclinic.features.users.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pja.aurorumclinic.features.users.dtos.response.GetDoctorResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final ObjectStorageService objectStorageService;

    @Override
    public List<GetDoctorResponse> getAllDoctors() throws IOException {
        List<Doctor> doctorsFromDb = doctorRepository.findAll();
        List<GetDoctorResponse> doctorsToReturn = new ArrayList<>();
        for (Doctor doctor : doctorsFromDb) {
            GetDoctorResponse responseDto = GetDoctorResponse.builder()
                    .id(doctor.getId())
                    .name(doctor.getName())
                    .surname(doctor.getSurname())
                    .pesel(doctor.getPesel())
                    .birthDate(doctor.getBirthdate())
                    .phoneNumber(doctor.getPhoneNumber())
                    .email(doctor.getEmail())
                    .description(doctor.getDescription())
                    .specialization(doctor.getSpecialization())
                    .profilePicture(objectStorageService.generateSignedUrl(doctor.getProfilePicture()))
                    .education(doctor.getEducation())
                    .experience(doctor.getExperience())
                    .pwzNumber(doctor.getPwzNumber())
                    .rating((int) doctor.getAppointments().stream()
                            .map(Appointment::getOpinion)
                            .filter(Objects::nonNull)
                            .mapToInt(Opinion::getRating)
                            .average()
                            .orElse(0.0))
                    .appointments(doctor.getAppointments())
                    .schedules(doctor.getSchedules())
                    .build();
            doctorsToReturn.add(responseDto);
        }
        return doctorsToReturn;
    }

    @Override
    public void uploadProfilePicture(MultipartFile image, Long doctorId) throws IOException {
        Doctor doctorFromDb = doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        String imagePath = objectStorageService.uploadObject(image);
        doctorFromDb.setProfilePicture(imagePath);
        doctorRepository.save(doctorFromDb);
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

}
