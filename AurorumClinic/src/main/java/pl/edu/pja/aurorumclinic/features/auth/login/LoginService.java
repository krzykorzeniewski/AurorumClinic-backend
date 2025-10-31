package pl.edu.pja.aurorumclinic.features.auth.login;

import pl.edu.pja.aurorumclinic.features.auth.login.dtos.*;

public interface LoginService {

    LoginUserResponse login(LoginUserRequest loginUserRequest);
    LoginUserResponse refresh(RefreshAccessTokenRequest refreshAccessTokenRequest);
    LoginUserResponse login2fa(TwoFactorAuthLoginRequest twoFactorAuthLoginRequest);
    void createMfaToken(TwoFactorAuthTokenRequest twoFactorAuthTokenRequest);

}
