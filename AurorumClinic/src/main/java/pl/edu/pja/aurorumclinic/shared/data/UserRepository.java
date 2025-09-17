package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);
    User findByRefreshToken(String refreshToken);
    User findByEmailVerificationToken(String emailVerificationToken);
    User findByPasswordResetToken(String token);
    User findByTwoFactorAuthToken(String token);

    User findByPhoneNumber(String phoneNumber);

    User findByPhoneNumberVerificationToken(String phoneNumberVerificationToken);

    boolean existsByEmail(String email);

    User findByIdAndEmailUpdateToken(Long id, String emailUpdateToken);
}
