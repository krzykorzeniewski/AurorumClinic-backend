package pl.edu.pja.aurorumclinic.features.auth.verify_phone_number;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class VerifyPhoneNumberController {

    private final VerifyPhoneNumberService verifyPhoneNumberService;

    @PostMapping("/verify-phone-number-token")
    public ResponseEntity<?> getVerifyPhoneNumberToken(@Valid @RequestBody VerifyPhoneNumberTokenRequest requestDto,
                                                       Authentication authentication) {
        verifyPhoneNumberService.sendVerifyPhoneNumberSms(requestDto, authentication);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/verify-phone-number")
    public ResponseEntity<?> verifyPhoneNumber(@Valid @RequestBody VerifyPhoneNumberRequest requestDto,
                                               Authentication authentication) {
        verifyPhoneNumberService.verifyPhoneNumber(requestDto, authentication);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
