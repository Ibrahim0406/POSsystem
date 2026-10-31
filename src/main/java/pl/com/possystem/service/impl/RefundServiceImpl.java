package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import pl.com.possystem.entity.Branch;
import pl.com.possystem.entity.Order;
import pl.com.possystem.entity.Refund;
import pl.com.possystem.entity.User;
import pl.com.possystem.mapper.RefundMapper;
import pl.com.possystem.payload.dto.RefundDTO;
import pl.com.possystem.repository.OrderRepository;
import pl.com.possystem.repository.RefundRepository;
import pl.com.possystem.service.RefundService;
import pl.com.possystem.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl  implements RefundService {

    private final RefundRepository refundRepository;

    private final UserService userService;
    private final OrderRepository orderRepository;

    @Override
    public RefundDTO createRefund(RefundDTO refund) throws Exception {
        User cashier  = userService.getCurrentUser();

        Order order = orderRepository.findById(refund.getOrderId()).orElseThrow(() -> new Exception("Order not found"));

        Branch branch = order.getBranch();

        Refund createdRefund = Refund.builder()
                .order(order)
                .cashier(cashier)
                .branch(branch)
                .reason(refund.getReason())
                .amount(refund.getAmount())
                .createdAt(LocalDateTime.now())
                .build();


        Refund savedRefund = refundRepository.save(createdRefund);
        return RefundMapper.toDTO(savedRefund);
    }

    @Override
    public List<RefundDTO> getAllRefunds() throws Exception {
        return refundRepository.findAll().stream().map(RefundMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<RefundDTO> getRefundByCashier(Long cashierId) throws Exception {
        return refundRepository.findByCashierId(cashierId).stream()
                .map(RefundMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<RefundDTO> getRefundByShiftReport(Long shiftReportId) throws Exception {
        return refundRepository.findByShiftReportId(shiftReportId).stream()
                .map(RefundMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public  List<RefundDTO> getRefundIdByCashierAndDateRange(Long cashierId, LocalDateTime startDate, LocalDateTime endDate) throws Exception {
        return refundRepository.findByCashierIdAndCreatedAtBetween(cashierId, startDate, endDate).stream()
                .map(RefundMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<RefundDTO> getRefundByBranch(Long branchId) throws Exception {
        return refundRepository.findByBranchId(branchId).stream()
                .map(RefundMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public RefundDTO getRefundById(Long refundId) throws Exception {
        return refundRepository.findById(refundId).map(RefundMapper::toDTO).orElseThrow(() -> new Exception("Refund not found"));
    }

    @Override
    public void deleteRefund(Long refundId) throws Exception {
        this.getRefundById(refundId);
        refundRepository.deleteById(refundId);
    }
}
