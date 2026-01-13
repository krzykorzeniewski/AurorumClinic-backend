package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Absence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Absence")
    private Long id;

    @Size(max = 100)
    @NotBlank
    @Column(name = "Name", columnDefinition = "nvarchar(100)")
    private String name;

    @Column(name = "Started_At", columnDefinition = "datetime2(2)")
    @NotNull(message = "This field is required")
    private LocalDateTime startedAt;

    @Column(name = "Finished_At", columnDefinition = "datetime2(2)")
    @NotNull(message = "This field is required")
    private LocalDateTime finishedAt;

    @ManyToOne
    @JoinColumn(name = "FK_Doctor")
    private Doctor doctor;

}
