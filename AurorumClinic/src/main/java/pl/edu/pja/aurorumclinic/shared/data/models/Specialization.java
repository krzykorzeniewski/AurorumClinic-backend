package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Specialization")
    private Long id;

    @Column(name = "Name", columnDefinition = "nvarchar(150)")
    @Size(max = 150, message = "Maximum length for this field is 150 characters")
    @NotBlank(message = "This field is required")
    private String name;

    @ManyToMany(mappedBy = "specializations")
    private Set<Doctor> doctors;

    @ManyToMany(mappedBy = "specializations")
    @ToString.Exclude
    private Set<Service> services;

}
