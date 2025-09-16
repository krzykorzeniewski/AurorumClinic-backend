package pl.edu.pja.aurorumclinic.features.users.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.UpdateUserEmailRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.UpdateUserPhoneNumberRequest;
import pl.edu.pja.aurorumclinic.features.users.services.UserService;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/{id}/email")
    public ResponseEntity<?> updatePatientEmail(@PathVariable Long id,
                                                @Valid @RequestBody UpdateUserEmailRequest requestDto,
                                                Authentication authentication) {
        userService.updateUserEmail(id, requestDto, authentication);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/{id}/phone-number")
    public ResponseEntity<?> updatePatientPhoneNumber(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateUserPhoneNumberRequest requestDto,
                                                      Authentication authentication) {
        userService.updateUserPhoneNumber(id, requestDto, authentication);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
