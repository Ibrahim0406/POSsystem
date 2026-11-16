package pl.com.possystem.entity;

import lombok.Data;
import pl.com.possystem.domain.PaymentType;

import java.math.BigDecimal;

@Data
public class PaymentSummary {

    private PaymentType type;

    private BigDecimal totalAmount;

    private Integer transactionCount;

    private Double percentage;
}
