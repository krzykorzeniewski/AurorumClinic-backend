package pl.edu.pja.aurorumclinic.features.users.doctors;

import pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared.DoctorProfileResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;

public class DoctorProfileMapper {
    public DoctorProfileResponse toResponse(Doctor d) {
        return new DoctorProfileResponse(
                d.getId(),
                d.getExperience(),
                d.getEducation(),
                d.getDescription(),
                d.getPwzNumber(),
                d.getProfilePicture()
        );
    }
}
