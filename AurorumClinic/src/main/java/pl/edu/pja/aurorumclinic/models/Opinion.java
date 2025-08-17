package pl.edu.pja.aurorumclinic.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
public class Opinion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Opinion")
    private Long id;

    @Min(value = 1, message = "Minimum value for this field is 1")
    @Max(value = 5, message = "Maximum value for this field is 5")
    @Column(name = "Rating")
    @NotNull(message = "This field is required")
    private int rating;

    @Column(name = "Comment", columnDefinition = "nvarchar(2000)")
    @Size(max = 2000, message = "Maximum length for this field is 2000 characters")
    @NotBlank(message = "This field is required")
    private String comment;

    @Column(name = "Answer", columnDefinition = "nvarchar(2000)")
    @Size(max = 2000, message = "Maximum length for this field is 2000 characters")
    private String answer;

    @Column(name = "Created_At", columnDefinition = "datetime2(2)")
    @NotNull(message = "This field is required")
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "opinion")
    private Appointment appointment;

}
