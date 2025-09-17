package pl.edu.pja.aurorumclinic.features.users.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.UpdateUserEmailRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.UpdateUserEmailTokenRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.UpdateUserPhoneNumberRequest;
import pl.edu.pja.aurorumclinic.features.users.services.UserService;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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

    @PutMapping("/{id}/phone-number")
    @PreAuthorize("#id == authentication.principal")
    public ResponseEntity<?> updateUserPhoneNumber(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateUserPhoneNumberRequest requestDto) {
        userService.updateUserPhoneNumber(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
