package pl.edu.pja.aurorumclinic.users.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.models.Appointment;
import pl.edu.pja.aurorumclinic.models.Doctor;
import pl.edu.pja.aurorumclinic.models.Opinion;
import pl.edu.pja.aurorumclinic.users.DoctorRepository;
import pl.edu.pja.aurorumclinic.users.dtos.response.GetDoctorResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    @Override
    public List<GetDoctorResponse> getAllDoctors() {
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
                    .profilePicture(doctor.getProfilePicture())
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
}
