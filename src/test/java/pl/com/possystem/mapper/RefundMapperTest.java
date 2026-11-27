package pl.com.possystem.mapper;

import org.junit.jupiter.api.Test;
import pl.com.possystem.entity.Branch;
import pl.com.possystem.entity.Order;
import pl.com.possystem.entity.Refund;
import pl.com.possystem.entity.User;

import static org.junit.jupiter.api.Assertions.assertNull;

class RefundMapperTest {

    @Test
    void mapsRefundWithoutShiftReport() {
        Order order = new Order();
        order.setId(10L);
        User cashier = new User();
        cashier.setFullName("Cashier");
        Branch branch = new Branch();
        branch.setId(20L);
        Refund refund = Refund.builder()
                .order(order)
                .cashier(cashier)
                .branch(branch)
                .build();

        assertNull(RefundMapper.toDTO(refund).getShiftReportId());
    }
}
