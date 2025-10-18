package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Newsletter_Message")
public class NewsletterMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Newsletter_Message")
    private Long id;

    @Column(name = "Text", columnDefinition = "nvarchar(500)")
    @Size(max = 500, message = "Required length for this field is 500 characters")
    @NotEmpty(message = "This field is required")
    private String text;

    @Column(name = "Created_At", columnDefinition = "datetime2(5)")
    @NotNull(message = "This field is required")
    private LocalDateTime createdAt;

    @Column(name = "Approved", columnDefinition = "bit")
    @NotNull
    private boolean approved = false;

    @Column(name = "Reviewed_At", columnDefinition = "datetime2(5)")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_Reviewer")
    private User reviewer;

    @OneToMany(mappedBy = "newsletterMessage")
    private List<PatientNewsletterMessage> patientNewsletterMessages;

}

