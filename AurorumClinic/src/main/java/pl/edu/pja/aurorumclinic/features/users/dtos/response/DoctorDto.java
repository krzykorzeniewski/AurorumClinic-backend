package pl.edu.pja.aurorumclinic.features.users.dtos.response;

public record DoctorDto(Long id,
                        String name,
                        String surname,
                        String profilePicture,
                        String specialization) {
}
