package pl.edu.pja.aurorumclinic.users.dtos;

import lombok.Builder;

@Builder
public record UserIdResponse(Long userId,
                             boolean twoFactorAuth) {
}
