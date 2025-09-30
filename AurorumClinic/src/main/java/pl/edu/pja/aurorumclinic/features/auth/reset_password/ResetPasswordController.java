package pl.edu.pja.aurorumclinic.features.auth.reset_password;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@PermitAll
public class ResetPasswordController {

    private final ResetPasswordService resetPasswordService;

    @PostMapping("/reset-password-token")
    public ResponseEntity<?> getResetPasswordToken(@Valid @RequestBody ResetPasswordTokenRequest requestDto) {
        resetPasswordService.sendResetPasswordEmail(requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest requestDto) {
        resetPasswordService.resetPassword(requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
