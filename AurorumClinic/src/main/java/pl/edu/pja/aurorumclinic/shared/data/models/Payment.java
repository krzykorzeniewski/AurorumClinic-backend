package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentMethod;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Payment")
    private Long id;

    @NotNull(message = "This field is required")
    @Column(name = "Amount", columnDefinition = "numeric(10,2)")
    private BigDecimal amount;

    @Column(name = "Created_At", columnDefinition = "datetime2(2)")
    @NotNull(message = "This field is required")
    private LocalDateTime createdAt;

    @Column(name = "Completed_At", columnDefinition = "datetime2(2)")
    private LocalDateTime completedAt;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "Method", length = 50, columnDefinition = "nvarchar(50)")
    private PaymentMethod method;

    @Enumerated(value = EnumType.STRING)
    @NotNull(message = "This field is required")
    @Column(name = "Status", length = 50, columnDefinition = "nvarchar(50)")
    private PaymentStatus status;

//    @OneToOne
//    @JoinColumn(name = "FK_Appointment", nullable = false, unique = true)
    @OneToOne(mappedBy = "payment")
    @ToString.Exclude
    private Appointment appointment;
}
