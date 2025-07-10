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

import java.time.LocalDate;

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

    @Column(name = "Name")
    @Size(max = 50)
    @NotBlank
    private String name;

    @Column(name = "Surname")
    @Size(max = 50)
    @NotBlank
    private String surname;

    @Column(name = "PESEL")
    @Size(min = 11, max = 11)
    @NotBlank
    private String pesel;

    @Column(name = "Birthdate")
    @NotNull
    private LocalDate birthdate;

    @Column(name = "Email")
    @Email
    @Size(max = 100)
    @NotBlank
    private String email;

    @Column(name = "Phone_Number")
    @Size(max = 11)
    @NotBlank
    private String phoneNumber;

    @OneToOne(mappedBy = "user")
    private Doctor doctor;

    @OneToOne(mappedBy = "user")
    private Patient patient;

}
