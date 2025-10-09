package pl.edu.pja.aurorumclinic.features.appointments.shared.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class DoctorDto {

    private final Long id;
    private final String name;
    private final String surname;
    private String profilePicture;
    private final String specialization;

}
