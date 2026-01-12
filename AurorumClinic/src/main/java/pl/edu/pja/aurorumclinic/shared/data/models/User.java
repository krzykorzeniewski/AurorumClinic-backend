package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "User_",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_email",
                        columnNames = {"Email"}
                ),
                @UniqueConstraint(
                        name = "uk_user_pesel",
                        columnNames = {"PESEL"}
                ),
                @UniqueConstraint(
                        name = "uk_user_phone_number",
                        columnNames = {"Phone_Number"}
                )
        })
@Inheritance(strategy = InheritanceType.JOINED)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_User")
    private Long id;

    @Column(name = "Name", columnDefinition = "nvarchar(50)")
    @Size(max = 50, message = "Maximum length for this field is 50 characters")
    @NotBlank(message = "This field is required")
    private String name;

    @Column(name = "Surname", columnDefinition = "nvarchar(50)")
    @Size(max = 50, message = "Maximum length for this field is 50 characters")
    @NotBlank(message = "This field is required")
    private String surname;

    @Column(name = "PESEL", columnDefinition = "nvarchar(11)")
    @Size(min = 11, max = 11, message = "Required length for this field is 11 characters")
    private String pesel;

    @Column(name = "Birthdate")
    @NotNull(message = "This field is required")
    private LocalDate birthdate;

    @Column(name = "Email", columnDefinition = "nvarchar(100)")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Maximum length for this field is 100 characters")
    @NotBlank(message = "This field is required")
    private String email;

    @Column(name = "Password", columnDefinition = "nvarchar(200)")
    @Size(max = 200, message = "Maximum length for this field is 200 characters")
    @NotBlank(message = "This field is required")
    private String password;

    @Column(name = "Phone_Number", columnDefinition = "nvarchar(9)")
    @Size(min = 9, max = 9, message = "Required length for this field is 9 characters")
    @NotBlank(message = "This field is required")
    private String phoneNumber;

    @Column(name = "Two_Factor_Authentication", columnDefinition = "bit")
    private boolean twoFactorAuth;

    @Column(name = "Role", length = 50, columnDefinition = "nvarchar(50)")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "This field is required")
    private UserRole role;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.REMOVE)
    private List<Message> sentMessages;

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.REMOVE)
    private List<Message> receivedMessages;

    @Column(name = "Email_Verified", columnDefinition = "bit")
    private boolean emailVerified = false;

    @Column(name = "Phone_Number_Verified", columnDefinition = "bit")
    private boolean phoneNumberVerified = false;

    @Column(name = "Pending_Email", columnDefinition = "nvarchar(100)")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Maximum length for this field is 100 characters")
    private String pendingEmail;

    @Column(name = "Pending_Phone_Number", columnDefinition = "nvarchar(9)")
    @Size(min = 9, max = 9, message = "Required length for this field is 9 characters")
    private String pendingPhoneNumber;

    @Column(name = "Created_At", columnDefinition = "datetime2(5)")
    @NotNull(message = "This field is required")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Token> tokens;

    @OneToMany(mappedBy = "reviewer")
    private List<NewsletterMessage> reviewedNewsletterMessages;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return String.valueOf(id);
    }

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
