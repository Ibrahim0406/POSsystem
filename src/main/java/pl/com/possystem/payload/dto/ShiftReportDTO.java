package pl.com.possystem.payload.dto;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import pl.com.possystem.entity.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ShiftReportDTO {

    private Long id;

    private LocalDateTime shiftStart;

    private LocalDateTime shiftEnd;

    private Double totalSales;

    private Double totalRefunds;

    private Double netSales;

    private Double totalOrders;


    private UserDTO cashier;

    private Long cashierId;

    private Long branchId;

    private BranchDTO branch;

    private List<PaymentSummary> paymentSummaries;

    private List<ProductDTO> topSellingProducts;

    private List<OrderDTO> recentOrders;

    private List<RefundDTO> refunds;
}
