package pl.edu.pja.aurorumclinic.features.users.dtos.response;

import lombok.Builder;

@Builder
public record SearchDoctorResponse(Long id,
                                   String name,
                                   String surname,
                                   String specialization,
                                   String profilePicture,
                                   int rating) {
}
