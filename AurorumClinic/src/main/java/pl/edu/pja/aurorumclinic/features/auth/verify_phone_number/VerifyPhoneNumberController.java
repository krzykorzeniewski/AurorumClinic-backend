package pl.edu.pja.aurorumclinic.features.auth.verify_phone_number;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/auth/me")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class VerifyPhoneNumberController {

    private final VerifyPhoneNumberService verifyPhoneNumberService;

    @PostMapping("/verify-phone-number-token")
    public ResponseEntity<ApiResponse<?>> getVerifyPhoneNumberToken(@AuthenticationPrincipal Long id) {
        verifyPhoneNumberService.sendVerifyPhoneNumberSms(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/verify-phone-number")
    public ResponseEntity<ApiResponse<?>> verifyPhoneNumber(@Valid @RequestBody VerifyPhoneNumberRequest requestDto,
                                               @AuthenticationPrincipal Long id) {
        verifyPhoneNumberService.verifyPhoneNumber(requestDto, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
