package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Entity
@Table(name = "Newsletter_Entry")
public class NewsletterEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Newsletter_Entry")
    private Long id;

    @Size(max = 200, message = "Maximum number of characters for this field is 200")
    @Column(name = "Subject", columnDefinition = "nvarchar(200)")
    @NotBlank(message = "This field is required")
    private String subject;

    @Size(max = 5000, message = "Maximum number of characters for this field is 200")
    @Column(name = "Content", columnDefinition = "nvarchar(5000)")
    private String content;

    @OneToMany(mappedBy = "newsletterEntry")
    private Set<PatientNewsletterEntry> patientNewsletterEntries;

}
