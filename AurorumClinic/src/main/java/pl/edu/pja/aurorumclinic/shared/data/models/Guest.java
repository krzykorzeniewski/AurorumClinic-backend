package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "Guest")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Guest")
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

    @Column(name = "Phone_Number", columnDefinition = "nvarchar(9)")
    @Size(min = 9, max = 9, message = "Required length for this field is 9 characters")
    @NotBlank(message = "This field is required")
    private String phoneNumber;

    @Column(name = "Appointment_Delete_Token", columnDefinition = "nvarchar(100)")
    @Size(max = 100, message = "Maximum length for this field is 100 characters")
    private String appointmentDeleteToken;

    @OneToOne(optional = false, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "FK_Appointment")
    private Appointment appointment;

}
