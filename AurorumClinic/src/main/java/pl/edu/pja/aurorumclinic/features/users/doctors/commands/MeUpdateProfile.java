package pl.edu.pja.aurorumclinic.features.users.doctors.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@Service
@RequiredArgsConstructor
public class MeUpdateProfile {

    private final DoctorRepository doctorRepository;

    @Transactional
    public Doctor handle(Long userID, MeUpdateProfileRequest req) {
        Doctor d = doctorRepository.findById(userID)
                .orElseThrow(() -> new ApiNotFoundException("ID not found", "Id"));

        if (req.experience() != null) d.setExperience(trimOrNull(req.experience()));
        if (req.education()  != null) d.setEducation(trimOrNull(req.education()));
        if (req.description() != null) d.setDescription(trimOrNull(req.description()));
        if (req.pwzNumber()  != null) d.setPwzNumber(trimOrNull(req.pwzNumber()));

        return d;
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
