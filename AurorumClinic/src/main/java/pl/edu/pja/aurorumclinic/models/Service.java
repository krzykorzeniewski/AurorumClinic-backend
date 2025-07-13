package pl.edu.pja.aurorumclinic.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Service")
    private Long id;

    @Size(max = 150)
    @NotBlank
    @Column(name = "Name")
    private String name;

    @Column(name = "Duration")
    @Min(1)
    @Max(180)
    private int duration;

    @NotNull
    @Column(name = "Price")
    private BigDecimal price;

    @Size(max = 500)
    @Column(name = "Description", columnDefinition = "nvarchar(500)")
    @NotBlank
    private String description;

    @OneToMany(mappedBy = "service")
    private List<Appointment> appointments;
}
