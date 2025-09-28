package pl.edu.pja.aurorumclinic.features.users.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface GetPatientAppointmentsResponse {

    Long getId();
    String getName();
    String getSurname();
    String getPesel();
    LocalDate getBirthdate();
    String getEmail();
    String getPhoneNumber();
    List<AppointmentDto> getAppointments();

    interface AppointmentDto {
        Long getId();
        LocalDateTime getStartedAt();
        String getDescription();
        DoctorDto getDoctor();
        ServiceDto getService();
    }

    interface DoctorDto {
        Long getId();
        String getName();
        String getSurname();
        String getProfilePicture();
    }

    interface ServiceDto {
        Long getId();
        String getName();

        @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
        BigDecimal getPrice();
    }
}
