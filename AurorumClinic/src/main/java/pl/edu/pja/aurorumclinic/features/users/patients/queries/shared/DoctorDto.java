package pl.edu.pja.aurorumclinic.features.users.patients.queries.shared;

public record DoctorDto(Long id,
                        String name,
                        String surname,
                        String profilePicture,
                        String specialization) {
}
