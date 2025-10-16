package pl.edu.pja.aurorumclinic.features.appointments.messages.queries;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/messages/me")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
public class MeGetAllConversations {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<GetConversationResponse>>> getAllMyConversationDtos(
            Authentication loggedInUser) {
        return ResponseEntity.ok(ApiResponse.success(handle(loggedInUser)));
    }

    private List<GetConversationResponse> handle(Authentication loggedInUser) {
        Long loggedInUserId = (Long) loggedInUser.getPrincipal();
        SimpleGrantedAuthority role = (SimpleGrantedAuthority) loggedInUser.getAuthorities().toArray()[0];
        UserRole roleAsEnum = UserRole.valueOf(role.getAuthority().substring(5));
        List<GetConversationResponse> response = null;
        if (Objects.equals(roleAsEnum, UserRole.PATIENT)) {
           response = doctorRepository.
                    findAllWhoHadConversationWithPatientId(loggedInUserId);
        } else {
            response = patientRepository.
                    findAllWhoHadConversationWithDoctorId(loggedInUserId);
        }
        return response;
    }

}
