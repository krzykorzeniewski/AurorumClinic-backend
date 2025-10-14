package pl.edu.pja.aurorumclinic.features.auth.register;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.auth.register.dtos.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@PermitAll
@RateLimiting(name = "sensitive")
public class RegisterController {

    private final RegisterService registerService;

    @PostMapping("/register-employee") //TODO generate password and send to employee email and ask to change, doctor same
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> registerEmployee(@Valid @RequestBody RegisterEmployeeRequest requestDto) {
        registerService.registerEmployee(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @PostMapping("/register-patient")
    public ResponseEntity<ApiResponse<?>> registerPatient(@Valid @RequestBody RegisterPatientRequest requestDto) {
        registerService.registerPatient(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @PostMapping("/register-doctor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> registerDoctor(@Valid @RequestBody RegisterDoctorRequest requestDto) {
        registerService.registerDoctor(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @PostMapping("/verify-email-token")
    public ResponseEntity<ApiResponse<?>> getVerifyEmailToken(@Valid @RequestBody VerifyEmailTokenRequest requestDto) {
        registerService.sendVerifyEmail(requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<?>> verifyEmail(@Valid @RequestBody VerifyEmailRequest requestDto) {
        registerService.verifyEmail(requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
