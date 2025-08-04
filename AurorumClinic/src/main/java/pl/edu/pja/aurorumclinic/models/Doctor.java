package pl.edu.pja.aurorumclinic.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
    @Size(max = 500)
    @NotBlank
    private String description;

    @Column(name = "Specialization", columnDefinition = "nvarchar(100)")
    @Size(max = 100)
    @NotBlank
    private String specialization;

    @Column(name = "Profile_Picture", columnDefinition = "varbinary(max)")
    @NotNull
    private byte[] profilePicture;

    @Column(name = "Education", columnDefinition = "nvarchar(100)")
    @Size(max = 100)
    @NotBlank
    private String education;

    @Column(name = "Experience", columnDefinition = "nvarchar(100)")
    @Size(max = 100)
    @NotBlank
    private String experience;

    @Column(name = "PWZ_Number", columnDefinition = "nvarchar(7)")
    @Size(min = 7, max = 7)
    @NotBlank
    private String pwzNumber;

    @OneToMany(mappedBy = "doctor")
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "doctor")
    private List<Schedule> schedules;

}
