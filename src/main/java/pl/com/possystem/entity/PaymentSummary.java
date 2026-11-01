package pl.com.possystem.entity;

import lombok.Data;
import pl.com.possystem.domain.PaymentType;

@Data
public class PaymentSummary {

    private PaymentType type;

    private Double totalAmount;

    private Integer transactionCount;

    private Double percentage;
}
