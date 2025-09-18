package pl.edu.pja.aurorumclinic.features.users.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.*;
import pl.edu.pja.aurorumclinic.features.users.services.UserService;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/{id}/2fa-token")
    @PreAuthorize("#id == authentication.principal")
    public ResponseEntity<?> set2faToken(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateUser2FATokenRequest requestDto) {
        userService.send2faSms(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/{id}/2fa")
    @PreAuthorize("#id == authentication.principal")
    public ResponseEntity<?> setUser2fa(@PathVariable Long id,
                                             @Valid @RequestBody UpdateUser2FARequest requestDto) {
        userService.updateUser2fa(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/email-update-token")
    @PreAuthorize("#id == authentication.principal")
    public ResponseEntity<?> updateUserEmailToken(@PathVariable Long id,
                                                @Valid @RequestBody UpdateUserEmailTokenRequest requestDto) {
        userService.sendUpdateEmail(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/{id}/email")
    @PreAuthorize("#id == authentication.principal")
    public ResponseEntity<?> updateUserEmail(@PathVariable Long id,
                                             @Valid @RequestBody UpdateUserEmailRequest requestDto) {
        userService.updateUserEmail(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/phone-number-update-token")
    @PreAuthorize("#id == authentication.principal")
    public ResponseEntity<?> updateUserPhoneNumberToken(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateUserPhoneNumberTokenRequest requestDto) {
        userService.sendUpdateSms(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/{id}/phone-number")
    @PreAuthorize("#id == authentication.principal")
    public ResponseEntity<?> updateUserPhoneNumber(@PathVariable Long id,
                                             @Valid @RequestBody UpdateUserPhoneNumberRequest requestDto) {
        userService.updateUserPhoneNumber(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
