package pl.com.possystem.entity;


import com.stripe.model.InvoicePayment;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "shift_reports")
public class ShiftReport {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime shiftStart;

    private LocalDateTime shiftEnd;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalSales;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalRefunds;

    @Column(precision = 19, scale = 2)
    private BigDecimal netSales;

    private Double totalOrders;

    @ManyToOne
    private User cashier;

    @ManyToOne
    private Branch branch;

    @Transient
    private List<PaymentSummary> paymentSummaries;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Product> topSellingProducts;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Order> recentOrders;

    @OneToMany(mappedBy = "shiftReport", cascade = CascadeType.ALL)
    private List<Refund> refunds;





}
