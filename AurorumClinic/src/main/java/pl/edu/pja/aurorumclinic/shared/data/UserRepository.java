package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    User findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

}
