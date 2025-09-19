package pl.edu.pja.aurorumclinic.features.auth.reset_password;

public interface ResetPasswordService {

    void sendResetPasswordEmail(ResetPasswordTokenRequest resetPasswordTokenRequest);

    void resetPassword(ResetPasswordRequest resetPasswordRequest);
}
