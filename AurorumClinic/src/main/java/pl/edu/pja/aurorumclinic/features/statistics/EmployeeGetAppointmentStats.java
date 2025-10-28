//package pl.edu.pja.aurorumclinic.features.statistics;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import pl.edu.pja.aurorumclinic.shared.ApiResponse;
//import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
//
//@RestController
//@RequestMapping("/api/stats")
//@RequiredArgsConstructor
//@PreAuthorize("hasAnyRole('DOCTOR', 'EMPLOYEE')")
//public class EmployeeGetAppointmentStats {
//
//    private final AppointmentRepository appointmentRepository;
//
//    public ResponseEntity<ApiResponse<GetAppointmentStatsResponse>> getAppointmentStatistics() {
//
//    }
//
//}
