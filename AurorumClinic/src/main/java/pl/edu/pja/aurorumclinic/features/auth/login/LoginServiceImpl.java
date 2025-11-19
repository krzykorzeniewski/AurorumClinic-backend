package pl.edu.pja.aurorumclinic.features.auth.login;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.auth.login.dtos.*;
import pl.edu.pja.aurorumclinic.features.auth.login.events.MfaTokenCreatedEvent;
import pl.edu.pja.aurorumclinic.features.auth.shared.ApiAuthenticationException;
import pl.edu.pja.aurorumclinic.shared.JwtUtils;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;
import pl.edu.pja.aurorumclinic.shared.services.TokenService;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final TokenService tokenService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${refresh-token-expiration-minutes}")
    private Integer refreshTokenExpirationInMinutes;

    @Value("${2fa-login-token.expiration.minutes}")
    private Integer mfaTokenExpirationInMinutes;

    @Override
    @Transactional
    public LoginUserResponse login(LoginUserRequest loginUserRequest) {
        User userFromDb = userRepository.findByEmail(loginUserRequest.email());
        if (userFromDb == null) {
            throw new ApiAuthenticationException("Invalid credentials", "credentials");
        }
        if (!passwordEncoder.matches(loginUserRequest.password(), userFromDb.getPassword())) {
            throw new ApiAuthenticationException("Invalid credentials", "credentials");
        }
        if (!userFromDb.isEmailVerified()) {
            throw new ApiAuthenticationException("Email is not verified", "email");
        }

        if (userFromDb.isTwoFactorAuth()) {
            return LoginUserResponse.builder()
                    .twoFactorAuth(userFromDb.isTwoFactorAuth())
                    .role(userFromDb.getRole())
                    .build();
        }

        String jwt = jwtUtils.createJwt(userFromDb);
        Token refreshToken = tokenService.createToken(userFromDb, TokenName.REFRESH, refreshTokenExpirationInMinutes);

        return LoginUserResponse.builder()
                .twoFactorAuth(userFromDb.isTwoFactorAuth())
                .role(userFromDb.getRole())
                .accessToken(jwt)
                .refreshToken(refreshToken.getRawValue())
                .build();
    }

    @Override
    @Transactional
    public LoginUserResponse refresh(RefreshAccessTokenRequest refreshAccessTokenRequest){
        String jwt = refreshAccessTokenRequest.accessToken();
        Long userId;
        try {
            userId = jwtUtils.getUserIdFromJwt(jwt);
        } catch (ExpiredJwtException e) {
            userId = jwtUtils.getUserIdFromExpiredJwt(jwt);
        } catch (JwtException jwtException) {
            throw new ApiAuthenticationException("Invalid token", "accessToken");
        }

        User userFromDb = userRepository.findById(userId).orElseThrow(
                () -> new ApiAuthenticationException("Invalid credentials", "credentials")
        );
        tokenService.validateAndDeleteToken(userFromDb, refreshAccessTokenRequest.refreshToken());

        String newJwt = jwtUtils.createJwt(userFromDb);
        Token newRefreshToken = tokenService.createToken(userFromDb, TokenName.REFRESH, refreshTokenExpirationInMinutes);

        return LoginUserResponse.builder()
                .twoFactorAuth(userFromDb.isTwoFactorAuth())
                .role(userFromDb.getRole())
                .accessToken(newJwt)
                .refreshToken(newRefreshToken.getRawValue())
                .build();
    }

    @Override
    @Transactional
    public LoginUserResponse login2fa (TwoFactorAuthLoginRequest twoFactorAuthLoginRequest){
        User userFromDb = userRepository.findByEmail(twoFactorAuthLoginRequest.email());
        if (userFromDb == null) {
            throw new ApiAuthenticationException("credentials", "email or token invalid");
        }
        tokenService.validateAndDeleteToken(userFromDb, twoFactorAuthLoginRequest.token());

        String jwt = jwtUtils.createJwt(userFromDb);
        Token refreshToken = tokenService.createToken(userFromDb, TokenName.REFRESH, refreshTokenExpirationInMinutes);

        return LoginUserResponse.builder()
                .twoFactorAuth(userFromDb.isTwoFactorAuth())
                .role(userFromDb.getRole())
                .accessToken(jwt)
                .refreshToken(refreshToken.getRawValue())
                .build();
    }

    @Override
    public void createMfaToken(TwoFactorAuthTokenRequest twoFactorAuthTokenRequest){
        User userFromDb = userRepository.findByEmail(twoFactorAuthTokenRequest.email());
        if (userFromDb == null) {
            throw new ApiAuthenticationException("Email not found", "email");
        }
        if (!userFromDb.isPhoneNumberVerified()) {
            throw new ApiAuthenticationException("Phone number is not verified", "phoneNumber");
        }
        if (!userFromDb.isTwoFactorAuth()) {
            throw new ApiAuthenticationException("Given email has 2fa disabled", "email");
        }
        Token token = tokenService.createOtpToken(userFromDb, TokenName.TWO_FACTOR_AUTH, mfaTokenExpirationInMinutes);
        applicationEventPublisher.publishEvent(new MfaTokenCreatedEvent(token, userFromDb));
    }

}
