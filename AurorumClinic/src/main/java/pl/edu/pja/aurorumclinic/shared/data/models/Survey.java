package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Survey")
    private Long id;

    @Min(value = 1, message = "Minimum value for this field is 1")
    @Max(value = 5, message = "Maximum value for this field is 5")
    @Column(name = "Grade")
    private Integer grade;

    @Column(name = "Created_At", columnDefinition = "datetime2(2)")
    @NotNull(message = "This field is required")
    private LocalDateTime createdAt;

    @Column(name = "Completed_At", columnDefinition = "datetime2(2)")
    private LocalDateTime completedAt;

    @Size(max = 300, message = "Maximum length for this field is 300 characters")
    @Column(name = "Comment", columnDefinition = "nvarchar(300)")
    private String comment;

    @OneToOne(mappedBy = "survey")
    @JoinColumn(name = "FK_Appointment")
    @ToString.Exclude
    private Appointment appointment;

}
