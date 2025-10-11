package pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared;

import lombok.Builder;

import java.util.List;

@Builder
public record GetDoctorResponse(
                    Long id,
                    String name,
                    String surname,
                    List<SpecializationDto> specializations,
                    String profilePicture,
                    int rating) {
    @Builder
    public record SpecializationDto(Long id,
                                    String name) {
    }

}
