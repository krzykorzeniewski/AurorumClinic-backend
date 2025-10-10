package pl.edu.pja.aurorumclinic.features.appointments.employees.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.appointments.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.dtos.GetAppointmentResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('EMPLOYEE', 'DOCTOR')")
public class GetAppointmentById {

    private final AppointmentRepository appointmentRepository;
    private final ObjectStorageService objectStorageService;


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetAppointmentResponse>> getAppointmentById(
            @PathVariable("id") Long appointmentId) {
        return ResponseEntity.ok(ApiResponse.success(handle(appointmentId)));
    }

    private GetAppointmentResponse handle(Long appointmentId) {
        GetAppointmentResponse response = appointmentRepository.getAppointmentById(appointmentId);
        if (response == null) {
            throw new ApiNotFoundException("Id not found", "id");
        }
        response.doctor().setProfilePicture(objectStorageService.generateUrl(response.doctor().getProfilePicture()));
        return response;
    }

}
