package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Service", uniqueConstraints = {
        @UniqueConstraint(name = "uk_service_name", columnNames = {"Name"})
})
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Service")
    private Long id;

    @Size(max = 150, message = "Maximum length for this field is 150 characters")
    @NotBlank(message = "This field is required")
    @Column(name = "Name", columnDefinition = "nvarchar(150)")
    private String name;

    @Column(name = "Duration")
    @Min(value = 1, message = "Minimum value for this field is 1")
    @Max(value = 180, message = "Maximum value for this field is 180")
    @NotNull(message = "This field is required")
    private int duration;

    @NotNull(message = "This field is required")
    @Column(name = "Price", columnDefinition = "numeric(10,2)")
    private BigDecimal price;

    @Size(max = 500, message = "Maximum length for this field is 500 characters")
    @Column(name = "Description", columnDefinition = "nvarchar(500)")
    @NotBlank(message = "This field is required")
    private String description;

    @OneToMany(mappedBy = "service")
    private List<Appointment> appointments;
}
