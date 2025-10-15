package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Appointment")
    private Long id;

    @Column(name = "Started_At", columnDefinition = "datetime2(2)")
    @NotNull(message = "This field is required")
    private LocalDateTime startedAt;

    @Column(name = "Finished_At", columnDefinition = "datetime2(2)")
    @NotNull(message = "This field is required")
    private LocalDateTime finishedAt;

    @Column(name = "Status", length = 50, columnDefinition = "nvarchar(50)")
    @Enumerated(value = EnumType.STRING)
    @NotNull(message = "This field is required")
    private AppointmentStatus status;

    @Column(name = "Description", columnDefinition = "nvarchar(500)")
    @Size(max = 500, message = "Maximum length for this field is 500 characters")
    @NotBlank(message = "This field is required")
    private String description;

    @Column(name = "Notification_Sent", columnDefinition = "bit")
    private boolean notificationSent = false;

    @OneToOne(mappedBy = "appointment")
    @ToString.Exclude
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_Service")
    @ToString.Exclude
    private Service service;

    @OneToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private Survey survey;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_Opinion")
    @ToString.Exclude
    private Opinion opinion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_Doctor")
    @ToString.Exclude
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_Patient")
    @ToString.Exclude
    private Patient patient;
}
