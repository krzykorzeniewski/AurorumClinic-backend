package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "Patient_Newsletter_Entry")
public class PatientNewsletterEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Patient_Newsletter_Entry")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "FK_Patient")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "FK_Newsletter_Entry")
    private NewsletterEntry newsletterEntry;

    @Column(name = "Sent_At", columnDefinition = "datetime2(5)")
    @NotNull(message = "This field is required")
    private LocalDateTime sentAt;

}
