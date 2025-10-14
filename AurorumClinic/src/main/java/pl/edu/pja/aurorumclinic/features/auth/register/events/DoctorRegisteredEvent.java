package pl.edu.pja.aurorumclinic.features.auth.register.events;

import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;

public record DoctorRegisteredEvent(Doctor doctor, String password) {
}
