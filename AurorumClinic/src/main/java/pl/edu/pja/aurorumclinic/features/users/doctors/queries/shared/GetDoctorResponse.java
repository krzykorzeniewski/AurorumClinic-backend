package pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@RequiredArgsConstructor
public class GetDoctorResponse {

    private final Long id;
    private final String name;
    private final String surname;
    private final List<SpecializationDto> specializations;
    private String profilePicture;
    private final int rating;

}
