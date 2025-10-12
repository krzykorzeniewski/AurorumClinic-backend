package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Schedule")
    private Long id;

    @Column(name = "Started_At", columnDefinition = "datetime2(2)")
    @NotNull(message = "This field is required")
    private LocalDateTime startedAt;

    @Column(name = "Finished_At", columnDefinition = "datetime2(2)")
    @NotNull(message = "This field is required")
    private LocalDateTime finishedAt;

    @ManyToOne
    @JoinColumn(name = "FK_Doctor")
    @ToString.Exclude
    private Doctor doctor;

    @ManyToMany
    @ToString.Exclude
    @JoinTable(name = "Service_Schedule",
        joinColumns = @JoinColumn(name = "PK_Schedule"),
        inverseJoinColumns = @JoinColumn(name = "PK_Service"))
    private Set<Service> services;
}
