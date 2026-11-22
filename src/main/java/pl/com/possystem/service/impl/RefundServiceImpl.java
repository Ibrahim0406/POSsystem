package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import pl.com.possystem.domain.UserRole;
import pl.com.possystem.entity.Branch;
import pl.com.possystem.entity.Order;
import pl.com.possystem.entity.Refund;
import pl.com.possystem.entity.ShiftReport;
import pl.com.possystem.entity.User;
import pl.com.possystem.mapper.RefundMapper;
import pl.com.possystem.payload.dto.RefundDTO;
import pl.com.possystem.repository.OrderRepository;
import pl.com.possystem.repository.BranchRepository;
import pl.com.possystem.repository.RefundRepository;
import pl.com.possystem.repository.ShiftReportRepository;
import pl.com.possystem.repository.UserRepository;
import pl.com.possystem.service.RefundService;
import pl.com.possystem.service.TenantAccess;
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
    private final BranchRepository branchRepository;
    private final ShiftReportRepository shiftReportRepository;
    private final UserRepository userRepository;
    private final TenantAccess tenantAccess;

    @Override
    public RefundDTO createRefund(RefundDTO refund) throws Exception {
        User cashier  = userService.getCurrentUser();

        Order order = orderRepository.findById(refund.getOrderId()).orElseThrow(() -> new Exception("Order not found"));

        Branch branch = order.getBranch();
        tenantAccess.requireBranchAccess(cashier, branch);

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
          User actor = userService.getCurrentUser();
          List<Refund> refunds;
          if (actor.getRole() == pl.com.possystem.domain.UserRole.ROLE_ADMIN) {
              refunds = refundRepository.findAll();
          } else if (actor.getRole() == pl.com.possystem.domain.UserRole.ROLE_BRANCH_MANAGER
                  || actor.getRole() == pl.com.possystem.domain.UserRole.ROLE_BRANCH_CASHIER) {
              if (actor.getBranch() == null) {
                  throw new org.springframework.security.access.AccessDeniedException(
                          "The current user is not assigned to a branch");
              }
              refunds = refundRepository.findByBranchId(actor.getBranch().getId()).stream()
                      .filter(refund -> actor.getRole() == UserRole.ROLE_BRANCH_MANAGER
                              || (refund.getCashier() != null
                              && java.util.Objects.equals(actor.getId(), refund.getCashier().getId())))
                      .collect(java.util.stream.Collectors.toList());
          } else {
              refunds = refundRepository.findByBranchStoreId(
                      tenantAccess.requireActorStore(actor).getId());
          }
          return refunds.stream()
                  .filter(refund -> {
                      tenantAccess.requireBranchAccess(actor, refund.getBranch());
                      return true;
                  })
                  .map(RefundMapper::toDTO)
                  .collect(Collectors.toList());
    }

    @Override
    public List<RefundDTO> getRefundByCashier(Long cashierId) throws Exception {
        User cashier = userRepository.findById(cashierId)
                .orElseThrow(() -> new Exception("Cashier not found"));
        tenantAccess.requireUserAccess(userService.getCurrentUser(), cashier);
        return refundRepository.findByCashierId(cashierId).stream()
                .map(RefundMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<RefundDTO> getRefundByShiftReport(Long shiftReportId) throws Exception {
        ShiftReport report = shiftReportRepository.findById(shiftReportId)
                .orElseThrow(() -> new Exception("Shift report not found"));
        User actor = userService.getCurrentUser();
        tenantAccess.requireBranchAccess(actor, report.getBranch());
        return refundRepository.findByShiftReportId(shiftReportId).stream()
                .filter(refund -> actor.getRole() != UserRole.ROLE_BRANCH_CASHIER
                        || (refund.getCashier() != null
                        && java.util.Objects.equals(actor.getId(), refund.getCashier().getId())))
                .map(RefundMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public  List<RefundDTO> getRefundIdByCashierAndDateRange(Long cashierId, LocalDateTime startDate, LocalDateTime endDate) throws Exception {
        User cashier = userRepository.findById(cashierId)
                .orElseThrow(() -> new Exception("Cashier not found"));
        tenantAccess.requireUserAccess(userService.getCurrentUser(), cashier);
        return refundRepository.findByCashierIdAndCreatedAtBetween(cashierId, startDate, endDate).stream()
                .map(RefundMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<RefundDTO> getRefundByBranch(Long branchId) throws Exception {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new Exception("Branch not found"));
        User actor = userService.getCurrentUser();
        tenantAccess.requireBranchAccess(actor, branch);
        return refundRepository.findByBranchId(branchId).stream()
                .filter(refund -> actor.getRole() != UserRole.ROLE_BRANCH_CASHIER
                        || (refund.getCashier() != null
                        && java.util.Objects.equals(actor.getId(), refund.getCashier().getId())))
                .map(RefundMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public RefundDTO getRefundById(Long refundId) throws Exception {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new Exception("Refund not found"));
        User actor = userService.getCurrentUser();
        tenantAccess.requireBranchAccess(actor, refund.getBranch());
        if (actor.getRole() == UserRole.ROLE_BRANCH_CASHIER
                && (refund.getCashier() == null
                || !java.util.Objects.equals(actor.getId(), refund.getCashier().getId()))) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Cashiers can only view their own refunds");
        }
        return RefundMapper.toDTO(refund);
    }

    @Override
    public void deleteRefund(Long refundId) throws Exception {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new Exception("Refund not found"));
        tenantAccess.requireBranchManagement(userService.getCurrentUser(), refund.getBranch());
        refundRepository.delete(refund);
    }
}
