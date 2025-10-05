package pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@Data
@RequiredArgsConstructor
public class RecommendedDoctorResponse {

    private final Long id;
    private final String name;
    private final String surname;
    private final String specialization;
    private String profilePicture;
    private final int rating;

}
