package pl.edu.pja.aurorumclinic.features.users.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.*;
import pl.edu.pja.aurorumclinic.features.users.services.UserService;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserService userService;

    @PostMapping("/me/2fa-token")
    public ResponseEntity<?> set2faToken(@AuthenticationPrincipal Long id) {
        userService.send2faUpdateSms(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/me/2fa")
    public ResponseEntity<?> setUser2fa(@AuthenticationPrincipal Long id,
                                         @Valid @RequestBody UpdateUser2FARequest requestDto) {
        userService.updateUser2fa(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/me/email-update-token")
    public ResponseEntity<?> updateUserEmailToken(@AuthenticationPrincipal Long id,
                                                  @Valid @RequestBody UpdateUserEmailTokenRequest requestDto) {
        userService.sendUpdateEmail(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/me/email")
    public ResponseEntity<?> updateUserEmail(@AuthenticationPrincipal Long id,
                                             @Valid @RequestBody UpdateUserEmailRequest requestDto) {
        userService.updateUserEmail(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/me/phone-number-update-token")
    public ResponseEntity<?> updateUserPhoneNumberToken(@AuthenticationPrincipal Long id,
                                                      @Valid @RequestBody UpdateUserPhoneNumberTokenRequest requestDto) {
        userService.sendUpdateSms(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/me/phone-number")
    public ResponseEntity<?> updateUserPhoneNumber(@AuthenticationPrincipal Long id,
                                             @Valid @RequestBody UpdateUserPhoneNumberRequest requestDto) {
        userService.updateUserPhoneNumber(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
