package pl.edu.pja.aurorumclinic.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@PrimaryKeyJoinColumn(name = "PK_Doctor")
public class Doctor extends User{

    @Column(name = "Description", columnDefinition = "nvarchar(500)")
    @Size(max = 500, message = "Maximum length for this field is 500 characters")
    @NotBlank(message = "This field is required")
    private String description;

    @Column(name = "Specialization", columnDefinition = "nvarchar(100)")
    @Size(max = 100, message = "Maximum length for this field is 100 characters")
    @NotBlank(message = "This field is required")
    private String specialization;

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

    @Column(name = "PWZ_Number", columnDefinition = "nvarchar(7)")
    @Size(min = 7, max = 7, message = "Required length for this field is 7 characters")
    @NotBlank(message = "This field is required")
    private String pwzNumber;

    @OneToMany(mappedBy = "doctor")
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "doctor")
    private List<Schedule> schedules;

}
