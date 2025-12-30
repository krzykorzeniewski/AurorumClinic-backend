package pl.edu.pja.aurorumclinic.features.absences.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.absences.queries.shared.EmployeeGetAbsenceResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/absences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeGetAllAbsences {

    private final AbsenceRepository absenceRepository;
    private final ObjectStorageService objectStorageService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<EmployeeGetAbsenceResponse>>> empGetAllAbsences(
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(handle(pageable)));
    }

    private Page<EmployeeGetAbsenceResponse> handle(Pageable pageable) {
        Page<Absence> absencesFromDb = absenceRepository.findAllPage(pageable);
        return absencesFromDb.map(absence -> EmployeeGetAbsenceResponse.builder()
                .name(absence.getName())
                .id(absence.getId())
                .startedAt(absence.getStartedAt())
                .finishedAt(absence.getFinishedAt())
                .doctor(EmployeeGetAbsenceResponse.DoctorDto.builder()
                        .id(absence.getDoctor().getId())
                        .name(absence.getDoctor().getName())
                        .surname(absence.getDoctor().getSurname())
                        .profilePicture(objectStorageService.generateUrl(absence.getDoctor().getProfilePicture()))
                        .specializations(absence.getDoctor().getSpecializations().stream()
                                .map(specialization -> EmployeeGetAbsenceResponse.DoctorDto
                                        .SpecializationDto.builder()
                                        .id(specialization.getId())
                                        .name(specialization.getName())
                                        .build()).toList())
                        .build())
                .build());
    }
}
