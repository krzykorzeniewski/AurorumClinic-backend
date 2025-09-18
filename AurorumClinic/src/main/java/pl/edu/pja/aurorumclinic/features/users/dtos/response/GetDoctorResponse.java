package pl.edu.pja.aurorumclinic.features.users.dtos.response;

import lombok.Builder;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;

import java.time.LocalDate;
import java.util.List;

@Builder
public record GetDoctorResponse(Long id,
                                String name,
                                String surname,
                                String pesel,
                                LocalDate birthDate,
                                String email,
                                String phoneNumber,
                                String description,
                                String specialization,
                                String profilePicture,
                                String education,
                                String experience,
                                String pwzNumber,
                                int rating,
                                List<Schedule> schedules,
                                List<Appointment> appointments) {
}
