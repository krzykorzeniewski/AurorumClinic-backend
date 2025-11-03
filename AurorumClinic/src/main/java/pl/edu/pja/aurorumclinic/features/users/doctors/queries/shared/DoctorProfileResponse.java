package pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared;

public record DoctorProfileResponse(
        Long doctorId,
        String experience,
        String education,
        String description,
        String pwzNumber,
        String profilePictureUrl
) {}
