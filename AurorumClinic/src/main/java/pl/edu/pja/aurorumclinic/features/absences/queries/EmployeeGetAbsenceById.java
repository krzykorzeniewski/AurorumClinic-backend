package pl.edu.pja.aurorumclinic.features.absences.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.absences.queries.shared.EmployeeGetAbsenceResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

@RestController
@RequestMapping("/api/absences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeGetAbsenceById {

    private final AbsenceRepository absenceRepository;
    private final ObjectStorageService objectStorageService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeGetAbsenceResponse>> empGetAbsenceById(@PathVariable("id") Long absenceId) {
        return ResponseEntity.ok(ApiResponse.success(handle(absenceId)));
    }

    private EmployeeGetAbsenceResponse handle(Long absenceId) {
        Absence absenceFromDb = absenceRepository.findById(absenceId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "absenceId")
        );
        return EmployeeGetAbsenceResponse.builder()
                .name(absenceFromDb.getName())
                .id(absenceFromDb.getId())
                .startedAt(absenceFromDb.getStartedAt())
                .finishedAt(absenceFromDb.getFinishedAt())
                .doctor(EmployeeGetAbsenceResponse.DoctorDto.builder()
                        .id(absenceFromDb.getDoctor().getId())
                        .name(absenceFromDb.getDoctor().getName())
                        .surname(absenceFromDb.getDoctor().getSurname())
                        .profilePicture(objectStorageService.generateUrl(absenceFromDb.getDoctor().getProfilePicture()))
                        .specializations(absenceFromDb.getDoctor().getSpecializations().stream()
                                .map(specialization -> EmployeeGetAbsenceResponse.DoctorDto
                                        .SpecializationDto.builder()
                                        .id(specialization.getId())
                                        .name(specialization.getName())
                                        .build()).toList())
                        .build())
                .build();
    }

}
