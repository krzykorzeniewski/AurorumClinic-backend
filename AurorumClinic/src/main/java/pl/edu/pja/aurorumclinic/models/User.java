package pl.edu.pja.aurorumclinic.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.edu.pja.aurorumclinic.models.enums.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "User_")
@Inheritance(strategy = InheritanceType.JOINED)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_User")
    private Long id;

    @Column(name = "Name", columnDefinition = "nvarchar(50)")
    @Size(max = 50)
    @NotBlank
    private String name;

    @Column(name = "Surname", columnDefinition = "nvarchar(50)")
    @Size(max = 50)
    @NotBlank
    private String surname;

    @Column(name = "PESEL", columnDefinition = "nvarchar(11)")
    @Size(min = 11, max = 11)
    private String pesel;

    @Column(name = "Birthdate")
    @NotNull
    private LocalDate birthdate;

    @Column(name = "Email", columnDefinition = "nvarchar(100)", unique = true)
    @Email
    @Size(max = 100)
    @NotBlank
    private String email;

    @Column(name = "Password", columnDefinition = "nvarchar(200)")
    @Size(max = 200)
    private String password;

    @Column(name = "Phone_Number", columnDefinition = "nvarchar(9)")
    @Size(min = 9, max = 9)
    @NotBlank
    private String phoneNumber;

    @Column(name = "Two_Factor_Authentication", columnDefinition = "bit")
    private boolean twoFactorAuth;

    @Column(name = "Refresh_Token", columnDefinition = "nvarchar(200)")
    @Size(max = 200)
    private String refreshToken;

    @Column(name = "Refresh_Token_Expiry_Date", columnDefinition = "datetime2(5)")
    private LocalDateTime refreshTokenExpiryDate;

    @Column(name = "Role", length = 50, columnDefinition = "nvarchar(50)")
    @Enumerated(EnumType.STRING)
    @NotNull
    private UserRole role;

    @OneToMany(mappedBy = "user")
    private List<Message> messages;

    @Column(name = "Email_Verified", columnDefinition = "bit")
    private boolean emailVerified = false;

    @Column(name = "Email_Verification_Token", columnDefinition = "nvarchar(100)")
    private String emailVerificationToken;

    @Column(name = "Email_Verification_Expiry_Date", columnDefinition = "datetime2(5)")
    private LocalDateTime emailVerificationExpiryDate;

    @Column(name = "Password_Reset_Token", columnDefinition = "nvarchar(100)")
    private String passwordResetToken;

    @Column(name = "Password_Reset_Expiry_Date", columnDefinition = "datetime2(5)")
    private LocalDateTime passwordResetExpiryDate;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }
}
