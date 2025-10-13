package pl.edu.pja.aurorumclinic.features.users.users.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UpdateUser { //admin & employee

    private final UserRepository userRepository;

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<UpdateUserResponse>> updateUserById(@PathVariable("id") Long userId,
                                                         @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(handle(userId, request)));
    }

    private UpdateUserResponse handle(Long userId, UpdateUserRequest request) {
        User userFromDb = userRepository.findById(userId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        userFromDb.setName(request.name);
        userFromDb.setSurname(request.surname);
        userFromDb.setPesel(request.pesel);
        userFromDb.setBirthdate(request.birthdate);
        userFromDb.setPhoneNumber(request.phoneNumber);
        userFromDb.setEmail(request.email);
        userFromDb.setTwoFactorAuth(request.twoFactorAuth);
        userFromDb.setPhoneNumberVerified(request.phoneNumberVerified);

        return UpdateUserResponse.builder()
                .id(userFromDb.getId())
                .name(userFromDb.getName())
                .surname(userFromDb.getSurname())
                .pesel(userFromDb.getPesel())
                .birthDate(userFromDb.getBirthdate())
                .phoneNumber(userFromDb.getPhoneNumber())
                .email(userFromDb.getEmail())
                .twoFactorAuth(userFromDb.isTwoFactorAuth())
                .phoneNumberVerified(userFromDb.isPhoneNumberVerified())
                .build();
    }

    @Builder
    record UpdateUserRequest(@NotBlank @Size(max = 50) String name,
                             @NotBlank @Size(max = 50) String surname,
                             @NotBlank @Size(min = 11, max = 11) String pesel,
                             @NotNull LocalDate birthdate,
                             @NotBlank @Size(min = 9, max = 9) String phoneNumber,
                             @NotBlank @Email @Size(max = 100) String email,
                             boolean twoFactorAuth,
                             boolean phoneNumberVerified) {

    }

    @Builder
    record UpdateUserResponse(Long id,
                              String name,
                              String surname,
                              String pesel,
                              LocalDate birthDate,
                              String phoneNumber,
                              String email,
                              boolean twoFactorAuth,
                              boolean phoneNumberVerified) {
    }

}
