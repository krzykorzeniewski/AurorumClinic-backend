package pl.edu.pja.aurorumclinic.shared.data.models.listeners;

import jakarta.persistence.PostLoad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DoctorListener {

    private final ObjectStorageService objectStorageService;

    @PostLoad
    public void postLoad(Doctor doctor) throws IOException {
        int rating = (int)doctor.getAppointments().stream()
                .map(Appointment::getOpinion)
                .filter(Objects::nonNull)
                .mapToInt(Opinion::getRating)
                .average()
                .orElse(0.0);
        doctor.setRating(rating);
        if (doctor.getProfilePicture() != null) {
            doctor.setProfilePicture(objectStorageService.generateSignedUrl(doctor.getProfilePicture()));
        }
    }
}
