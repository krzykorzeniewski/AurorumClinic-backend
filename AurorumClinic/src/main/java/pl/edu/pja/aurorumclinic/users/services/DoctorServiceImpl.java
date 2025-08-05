package pl.edu.pja.aurorumclinic.users.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.pja.aurorumclinic.models.Doctor;
import pl.edu.pja.aurorumclinic.users.DoctorRepository;
import pl.edu.pja.aurorumclinic.users.dtos.GetDoctorResponseDto;
import pl.edu.pja.aurorumclinic.users.shared.ResourceNotFoundException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    @Override
    public Doctor uploadProfilePicture(Long id, MultipartFile picture) throws IOException {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor id: " + id + " does not exist"));

        doctor.setProfilePicture(Base64.getEncoder().encodeToString(picture.getBytes()));
        return doctorRepository.save(doctor);
    }

    @Override
    public List<GetDoctorResponseDto> getAllDoctors() {
        List<Doctor> doctorsFromDb = doctorRepository.findAll();
        List<GetDoctorResponseDto> doctorsToReturn = new ArrayList<>();
        for (Doctor doctor : doctorsFromDb) {
            GetDoctorResponseDto responseDto = GetDoctorResponseDto.builder()
                    .id(doctor.getId())
                    .name(doctor.getName())
                    .surname(doctor.getSurname())
                    .pesel(doctor.getPesel())
                    .birthDate(doctor.getBirthdate())
                    .phoneNumber(doctor.getPhoneNumber())
                    .email(doctor.getEmail())
                    .description(doctor.getDescription())
                    .specialization(doctor.getSpecialization())
                    .profilePicture(doctor.getProfilePicture())
                    .education(doctor.getEducation())
                    .experience(doctor.getExperience())
                    .pwzNumber(doctor.getPwzNumber())
                    .appointments(doctor.getAppointments())
                    .schedules(doctor.getSchedules())
                    .build();
            doctorsToReturn.add(responseDto);
        }
        return doctorsToReturn;
    }
}
