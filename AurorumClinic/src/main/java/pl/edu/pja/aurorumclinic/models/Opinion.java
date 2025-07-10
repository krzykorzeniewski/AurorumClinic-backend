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

    @Min(1)
    @Max(5)
    @Column(name = "Rating")
    private int rating;

    @Column(name = "Comment")
    @Size(min = 1, max = 800)
    @NotBlank
    private String comment;

    @Column(name = "Answer")
    @Size(min = 1, max = 800)
    private String answer;

    @Column(name = "Created_At", columnDefinition = "datetime2(2)")
    @NotNull
    private LocalDateTime createdAt;

    @OneToOne
    private Appointment appointment;

}
