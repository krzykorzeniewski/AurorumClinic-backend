package pl.edu.pja.aurorumclinic.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pja.aurorumclinic.models.enums.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "User_")
public class User {

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

    @Column(name = "Email", columnDefinition = "nvarchar(100)")
    @Email
    @Size(max = 100)
    @NotBlank
    private String email;

    @Column(name = "Password", columnDefinition = "nvarchar(200)")
    @Size(max = 200)
    private String password;

    @Column(name = "Phone_Number", columnDefinition = "nvarchar(11)")
    @Size(max = 11)
    @NotBlank
    private String phoneNumber;

    @Column(name = "Salt", columnDefinition = "nvarchar(20)")
    @Size(min = 20, max = 20)
    private String salt;

    @Column(name = "Two_Factor_Authentication", columnDefinition = "bit")
    private boolean twoFactorAuth;

    @Column(name = "Refresh_Token")
    @Size(min = 20, max = 20)
    private String refreshToken;

    @Column(name = "Refresh_Token_Expiry_Date", columnDefinition = "datetime2(5)")
    private LocalDateTime refreshTokenExpiryDate;

    @Column(name = "Role", length = 50, columnDefinition = "nvarchar(50)")
    @Enumerated(EnumType.STRING)
    @NotNull
    private UserRole role;

    @OneToOne(mappedBy = "user")
    private Doctor doctor;

    @OneToOne(mappedBy = "user")
    private Patient patient;

    @OneToMany(mappedBy = "user")
    private List<Message> messages;

}
