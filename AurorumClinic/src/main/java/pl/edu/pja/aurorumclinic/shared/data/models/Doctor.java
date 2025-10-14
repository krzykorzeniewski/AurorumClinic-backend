package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@PrimaryKeyJoinColumn(name = "PK_Doctor")
@Table(name = "Doctor",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_doctor_pwz_number",
                columnNames = {"PWZ_Number"}
        ))
public class Doctor extends User {
    @Column(name = "Description", columnDefinition = "nvarchar(500)")
    @Size(max = 500, message = "Maximum length for this field is 500 characters")
    @NotBlank(message = "This field is required")
    private String description;

    @Column(name = "Profile_Picture", columnDefinition = "nvarchar(max)")
    private String profilePicture;

    @Column(name = "Education", columnDefinition = "nvarchar(100)")
    @Size(max = 100, message = "Maximum length for this field is 100 characters")
    @NotBlank(message = "This field is required")
    private String education;

    @Column(name = "Experience", columnDefinition = "nvarchar(100)")
    @Size(max = 100, message = "Maximum length for this field is 100 characters")
    @NotBlank(message = "This field is required")
    private String experience;

    @Column(name = "PWZ_Number", columnDefinition = "nvarchar(50)")
    @Size( max = 50, message = "Maximum length for this field is 50 characters")
    private String pwzNumber;

    @OneToMany(mappedBy = "doctor")
    @ToString.Exclude
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "doctor")
    @ToString.Exclude
    private Set<Schedule> schedules;

    @ManyToMany
    @ToString.Exclude
    @JoinTable(name = "Specialization_Doctor",
            joinColumns = @JoinColumn(name = "PK_Doctor"),
            inverseJoinColumns = @JoinColumn(name = "PK_Specialization"))
    private Set<Specialization> specializations;

}
