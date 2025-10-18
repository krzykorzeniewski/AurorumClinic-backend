package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Patient_Newsletter_Message")
public class PatientNewsletterMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Patient_Newsletter_Message")
    private Long id;

    @Column(name = "Sent_At", columnDefinition = "datetime2(5)")
    @NotNull
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_Newsletter_Message")
    private NewsletterMessage newsletterMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_Patient")
    private Patient patient;

}
