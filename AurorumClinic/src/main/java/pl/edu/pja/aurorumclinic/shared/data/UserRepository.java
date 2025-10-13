package pl.edu.pja.aurorumclinic.shared.data;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.features.users.users.queries.shared.GetUserResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("""
            select new pl.edu.pja.aurorumclinic.features.users.users.queries.shared.GetUserResponse(
            u.id, u.name, u.surname, u.pesel, u.birthdate, u.email, u.phoneNumber, u.twoFactorAuth, u.emailVerified,
            u.phoneNumberVerified, u.role, u.createdAt
                        ) from User u
            where (:role is null or u.role = :role)
            """)
    Page<GetUserResponse> findAllUserResponseDtos(Pageable pageable, UserRole role);

    @Query("""
            select new pl.edu.pja.aurorumclinic.features.users.users.queries.shared.GetUserResponse(
            u.id, u.name, u.surname, u.pesel, u.birthdate, u.email, u.phoneNumber, u.twoFactorAuth, u.emailVerified,
            u.phoneNumberVerified, u.role, u.createdAt
                        ) from User u
            where u.id = :userId
            """)
    GetUserResponse findUserResponseDtoById(Long userId);

    @Query("""
            select new pl.edu.pja.aurorumclinic.features.users.users.queries.shared.GetUserResponse(
            u.id, u.name, u.surname, u.pesel, u.birthdate, u.email, u.phoneNumber, u.twoFactorAuth, u.emailVerified,
            u.phoneNumberVerified, u.role, u.createdAt
                        ) from User u
            where (:role is null or u.role = :role) and (
            lower(u.name) like lower(concat('%', :query, '%')) or
            lower(u.surname) like lower(concat('%', :query, '%')) or
            lower(u.email) like lower(concat('%', :query, '%')) or
            lower(u.pesel) like lower(concat('%', :query, '%')) or
            lower(u.phoneNumber) like lower(concat('%', :query, '%')))
            """)
    Page<GetUserResponse> searchAllUserResponseDtos(Pageable pageable, String query, UserRole role);
}
