package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

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

    @Column(name = "Started_At", columnDefinition = "datetime2(2)")
    @NotNull(message = "This field is required")
    private LocalDateTime startedAt;

    @Column(name = "Finished_At", columnDefinition = "datetime2(2)")
    @NotNull(message = "This field is required")
    private LocalDateTime finishedAt;

    @ManyToOne
    @JoinColumn(name = "FK_Doctor")
    private Doctor doctor;

    @ManyToMany
    @JoinTable(name = "Service_Schedule",
        joinColumns = @JoinColumn(name = "PK_Schedule"),
        inverseJoinColumns = @JoinColumn(name = "PK_Service"))
    private Set<Service> services;
}
