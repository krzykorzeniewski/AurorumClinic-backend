package pl.edu.pja.aurorumclinic.features.auth.dtos.response;

import lombok.Builder;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;

@Builder
public record GetBasicUserInfoResponse(Long userId,
                                       String email,
                                       boolean twoFactorAuth,
                                       UserRole role) {
}
