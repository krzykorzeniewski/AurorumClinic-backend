package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.TokenName;

import java.time.LocalDateTime;

@Entity
@Table(name = "Token",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"FK_User", "Name"})
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Token")
    private Long id;

    @Column(name = "Value", columnDefinition = "nvarchar(100)", unique = true)
    @Size(max = 100, message = "Maximum length for this field is 100 characters")
    @NotBlank
    private String value;

    @Column(name = "Expiry_Date", columnDefinition = "datetime2(5)")
    @NotNull
    private LocalDateTime expiryDate;

    @Column(name = "Name", length = 100, columnDefinition = "nvarchar(100)")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "This field is required")
    private TokenName name;

    @Transient
    private String rawValue;

    @ManyToOne(optional = false)
    @JoinColumn(name = "FK_User", nullable = false)
    private User user;

}
