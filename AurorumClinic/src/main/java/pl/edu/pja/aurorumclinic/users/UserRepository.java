package pl.edu.pja.aurorumclinic.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.models.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);
    User findByRefreshToken(String refreshToken);
    User findByEmailVerificationToken(String emailVerificationToken);
    User findByPasswordResetToken(String token);
    User findByTwoFactorAuthToken(String token);
}
