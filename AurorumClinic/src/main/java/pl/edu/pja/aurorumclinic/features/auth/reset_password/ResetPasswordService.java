package pl.edu.pja.aurorumclinic.features.auth.reset_password;

public interface ResetPasswordService {

    void createResetPasswordToken(ResetPasswordTokenRequest resetPasswordTokenRequest);

    void resetPassword(ResetPasswordRequest resetPasswordRequest);
}
