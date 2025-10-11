package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Specialization",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_specialization_name",
                columnNames = {"Name"}
        ))
public class Specialization {

    @Id
    @Column(name = "PK_Specialization")
    private Long id;

    @Column(name = "Name", columnDefinition = "nvarchar(150)")
    @Size(max = 150, message = "Maximum length for this field is 150 characters")
    @NotBlank(message = "This field is required")
    private String name;

    @ManyToMany
    @JoinTable(name = "Specialization_Doctor",
            joinColumns = @JoinColumn(name = "PK_Specialization"),
            inverseJoinColumns = @JoinColumn(name = "PK_Doctor"))
    private Set<Doctor> doctors;

    @ManyToMany
    @JoinTable(name = "Specialization_Service",
            joinColumns = @JoinColumn(name = "PK_Specialization"),
            inverseJoinColumns = @JoinColumn(name = "PK_Service"))
    private Set<Service> services;

}
