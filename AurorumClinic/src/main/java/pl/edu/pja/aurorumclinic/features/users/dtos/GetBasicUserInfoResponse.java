package pl.edu.pja.aurorumclinic.features.users.dtos;

import lombok.Builder;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;

@Builder
public record GetBasicUserInfoResponse(Long userId,
                                       boolean twoFactorAuth,
                                       UserRole role) {
}
