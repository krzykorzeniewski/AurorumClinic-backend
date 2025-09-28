package pl.edu.pja.aurorumclinic.features.users.dtos.response;

import lombok.Builder;

@Builder
public record DoctorDto(Long id,
                 String name,
                 String surname,
                 String profilePicture) {
}
