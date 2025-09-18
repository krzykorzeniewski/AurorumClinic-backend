package pl.edu.pja.aurorumclinic.features.auth.verify_phone_number;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.auth.shared.ApiAuthException;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.TokenRepository;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

import java.time.LocalDateTime;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth/verify-phone-number")
@RequiredArgsConstructor
public class VerifyPhoneNumberHandler {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/verify-phone-number")
    @Transactional
    public ResponseEntity<?> verifyPhoneNumber(@Valid @RequestBody VerifyPhoneNumberRequest requestDto,
                                               Authentication authentication) {
        User userFromDb = userRepository.findByPhoneNumber(requestDto.phoneNumber());
        if (userFromDb == null) {
            throw new ApiAuthException("Phone number not found", "phoneNumber");
        }
        if (!Objects.equals(userFromDb.getId(), authentication.getPrincipal())) {
            throw new AuthorizationDeniedException("Access denied");
        }
        tokenService.validateAndDeleteToken(userFromDb, requestDto.token);
        userFromDb.setPhoneNumberVerified(true);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    public record VerifyPhoneNumberRequest(@NotBlank @Size(max = 6, min = 6) String token,
                                           @NotBlank @Size(max = 9, min = 9) String phoneNumber) {
    }

}
