package pl.edu.pja.aurorumclinic.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Schedule")
    private Long id;

    @Column(name = "Date")
    @NotNull
    private LocalDate date;

    @Column(name = "Started_At", columnDefinition = "datetime2(2)")
    @NotNull
    private LocalDateTime startedAt;

    @Column(name = "Finished_At", columnDefinition = "datetime2(2)")
    @NotNull
    private LocalDateTime finishedAt;

    @ManyToOne
    @JoinColumn(name = "FK_Doctor")
    private Doctor doctor;
}
