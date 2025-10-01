package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
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

    @OneToOne(mappedBy = "appointment")
    private Payment payment;

    @ManyToOne
    @JoinColumn(name = "FK_Service")
    private Service service;

    @OneToOne(mappedBy = "appointment")
    private Survey survey;

    @OneToOne
    @JoinColumn(name = "FK_Opinion")
    private Opinion opinion;

    @ManyToOne
    @JoinColumn(name = "FK_Doctor")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "FK_Patient")
    private Patient patient;

    @OneToMany(mappedBy = "appointment")
    private List<Message> messages;
}
