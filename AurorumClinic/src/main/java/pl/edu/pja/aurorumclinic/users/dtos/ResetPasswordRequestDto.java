package pl.edu.pja.aurorumclinic.users.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(@Size(max = 200) @NotBlank String password,
                                      @Size(max = 100) @NotBlank String token) {
}
