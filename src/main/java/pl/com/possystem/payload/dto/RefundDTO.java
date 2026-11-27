package pl.com.possystem.payload.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import pl.com.possystem.domain.PaymentType;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundDTO {


    private Long id;

    private String reason;

    private BigDecimal amount;

    private OrderDTO order;

    private Long orderId;

   // private ShiftReport shiftReport;

    private Long shiftReportId;

    private UserDTO cashier;

    private String cashierName;

    private BranchDTO branch;

    private Long branchId;

    private LocalDateTime createdAt;

    private PaymentType paymentType;
}
