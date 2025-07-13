package pl.edu.pja.aurorumclinic.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Survey")
    private Long id;

    @Min(1)
    @Max(5)
    @Column(name = "Grade")
    private int grade;

    @Column(name = "Sent_At", columnDefinition = "datetime2(2)")
    @NotNull
    private LocalDateTime sentAt;

    @Column(name = "Completed_At", columnDefinition = "datetime2(2)")
    private LocalDateTime completedAt;

    @Size(max = 300)
    @Column(name = "Comment", columnDefinition = "nvarchar(300)")
    private String comment;

    @OneToOne(optional = false)
    @JoinColumn(name = "FK_Appointment")
    private Appointment appointment;

}
