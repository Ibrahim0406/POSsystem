package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import pl.com.possystem.domain.UserRole;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.com.possystem.domain.PaymentType;
import pl.com.possystem.entity.*;
import pl.com.possystem.mapper.ShiftReportMapper;
import pl.com.possystem.payload.dto.ShiftReportDTO;
import pl.com.possystem.repository.*;
import pl.com.possystem.service.ShiftReportService;
import pl.com.possystem.service.TenantAccess;
import pl.com.possystem.service.UserService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftReportServiceImpl implements ShiftReportService {

    private final ShiftReportRepository shiftReportRepository;

    private final UserService userService;
    private final BranchRepository branchRepository;
    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final TenantAccess tenantAccess;

    @Override
    public ShiftReportDTO startShift() throws Exception {

        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() != UserRole.ROLE_BRANCH_CASHIER) {
            throw new AccessDeniedException("Only branch cashiers can start a shift");
        }
        Branch branch = currentUser.getBranch();
        if (branch == null) {
            throw new Exception("The current user is not assigned to a branch and cannot start a shift");
        }
        tenantAccess.requireBranchAccess(currentUser, branch);

        LocalDateTime shiftStart = LocalDateTime.now();
        LocalDateTime startOfDay =shiftStart.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = shiftStart.withHour(23).withMinute(59).withSecond(59);

        Optional<ShiftReport> exsisting = shiftReportRepository.findByCashierAndShiftStartBetween(currentUser, startOfDay, endOfDay);

        if (exsisting.isPresent()) {
            throw new Exception("Shift already started for today");
        }

        ShiftReport shiftReport = ShiftReport.builder()
                .cashier(currentUser)
                .branch(branch)
                .shiftStart(shiftStart)
                .build();

        ShiftReport savedShiftReport = shiftReportRepository.save(shiftReport);

        return ShiftReportMapper.toDTO( savedShiftReport);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShiftReportDTO endShift(Long shiftReportId, LocalDateTime shiftEnd) throws Exception {

        User user = userService.getCurrentUser();
        if (user.getRole() != UserRole.ROLE_BRANCH_CASHIER) {
            throw new AccessDeniedException("Only branch cashiers can end their own shift");
        }

        ShiftReport shiftReport = getActiveShiftForUpdate(user);
        tenantAccess.requireBranchAccess(user, shiftReport.getBranch());

        LocalDateTime effectiveShiftEnd = shiftEnd == null ? LocalDateTime.now() : shiftEnd;
        if (!effectiveShiftEnd.isAfter(shiftReport.getShiftStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shift end must be after shift start");
        }
        shiftReport.setShiftEnd(effectiveShiftEnd);

        List<Refund> refunds = findRefundsForShift(shiftReport, effectiveShiftEnd);

        BigDecimal totalRefunds = refunds.stream()
                .map(Refund::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Order> orders = orderRepository.findByCashierAndCreatedAtBetween(
                user, shiftReport.getShiftStart(), shiftReport.getShiftEnd()
        );

        BigDecimal totalSales = orders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double totalOrders = orders.size();

        BigDecimal netSales = totalSales.subtract(totalRefunds);


        shiftReport.setTotalRefunds(totalRefunds);
        shiftReport.setTotalSales(totalSales);
        shiftReport.setTotalOrders(totalOrders);
        shiftReport.setNetSales(netSales);
        shiftReport.setRecentOrders(getRecentOrders(orders));
        shiftReport.setTopSellingProducts(getTopSellingProducts(orders));
        shiftReport.setPaymentSummaries(getPaymentSummaries(orders, totalSales));
        shiftReport.setRefunds(refunds);

       ShiftReport savedShiftReport = shiftReportRepository.save(shiftReport);

        return ShiftReportMapper.toDTO(savedShiftReport);
    }



    @Override
    public ShiftReportDTO getShiftReportById(Long id) throws Exception {
        ShiftReport report = shiftReportRepository.findById(id)
                .orElseThrow(() -> new Exception("Shift report not found"));
        User actor = userService.getCurrentUser();
        tenantAccess.requireBranchAccess(actor, report.getBranch());
        if (actor.getRole() == UserRole.ROLE_BRANCH_CASHIER
                && (report.getCashier() == null
                || !java.util.Objects.equals(actor.getId(), report.getCashier().getId()))) {
            throw new AccessDeniedException("Cashiers can only view their own shift reports");
        }
        return ShiftReportMapper.toDTO(report);
    }

    @Override
    public List<ShiftReportDTO> getAllShiftReports() throws Exception {
        User actor = userService.getCurrentUser();
        List<ShiftReport> shiftReports;
        if (actor.getRole() == UserRole.ROLE_ADMIN) {
            shiftReports = shiftReportRepository.findAll();
        } else if (actor.getRole() == UserRole.ROLE_BRANCH_MANAGER
                || actor.getRole() == UserRole.ROLE_BRANCH_CASHIER) {
            if (actor.getBranch() == null) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "The current user is not assigned to a branch");
            }
            shiftReports = shiftReportRepository.findByBranchId(actor.getBranch().getId());
            if (actor.getRole() == UserRole.ROLE_BRANCH_CASHIER) {
                shiftReports = shiftReports.stream()
                        .filter(report -> report.getCashier() != null
                                && java.util.Objects.equals(actor.getId(), report.getCashier().getId()))
                        .toList();
            }
        } else {
            shiftReports = shiftReportRepository.findByBranchStoreId(
                    tenantAccess.requireActorStore(actor).getId());
        }
        shiftReports = shiftReports.stream()
                .filter(report -> {
                    tenantAccess.requireBranchAccess(actor, report.getBranch());
                    return true;
                })
                .toList();
        return shiftReports.stream().map(ShiftReportMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ShiftReportDTO> getShiftReportsByBranchId(Long branchId) throws Exception {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Branch not found"));
        User actor = userService.getCurrentUser();
        tenantAccess.requireBranchAccess(actor, branch);
        List<ShiftReport> shiftReports = shiftReportRepository.findByBranchId(branchId);
        if (actor.getRole() == UserRole.ROLE_BRANCH_CASHIER) {
            shiftReports = shiftReports.stream()
                    .filter(report -> report.getCashier() != null
                            && java.util.Objects.equals(actor.getId(), report.getCashier().getId()))
                    .toList();
        }
        return shiftReports.stream().map(ShiftReportMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ShiftReportDTO> getShiftReportsByCashierId(Long cashierId) throws Exception {
        User cashier = userRepository.findById(cashierId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Cashier not found"));
        tenantAccess.requireUserAccess(userService.getCurrentUser(), cashier);
        List<ShiftReport> shiftReports = shiftReportRepository.findByCashierId(cashierId);
        return shiftReports.stream().map(ShiftReportMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public ShiftReportDTO getCurrentShiftProgress(Long cashierId) throws Exception {
        User actor = userService.getCurrentUser();
        User cashier = actor;
        if (cashierId != null) {
            cashier = userRepository.findById(cashierId)
                    .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Cashier not found"));
            tenantAccess.requireUserAccess(actor, cashier);
        }
        tenantAccess.requireBranchAccess(actor, cashier.getBranch());

        ShiftReport shiftReport = shiftReportRepository
                .findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(cashier)
                .orElseThrow(() -> new Exception("No active shift found for the cashier"));

        return buildActiveShiftProgress(shiftReport);
    }

    @Override
    public ShiftReportDTO getShiftByCashierAndDate(Long cashierId, LocalDateTime date) throws Exception {

        User cashier = userRepository.findById(cashierId).orElseThrow(() -> new Exception("Cashier not found"));
        tenantAccess.requireUserAccess(userService.getCurrentUser(), cashier);

        LocalDateTime start = date.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = date.withHour(23).withMinute(59).withSecond(59);

        ShiftReport report = shiftReportRepository.findByCashierAndShiftStartBetween(cashier, start, end)
                .orElseThrow(() -> new Exception("No shift report found for the given cashier and date"));
        tenantAccess.requireBranchAccess(userService.getCurrentUser(), report.getBranch());

        return ShiftReportMapper.toDTO(report);
    }

    @Override
    public List<ShiftReportDTO> getCurrentBranchActiveShiftProgress() throws Exception {
        User manager = userService.getCurrentUser();

        if (manager.getRole() != UserRole.ROLE_BRANCH_MANAGER) {
            throw new AccessDeniedException("Only branch managers can view branch shifts");
        }

        Branch branch = manager.getBranch();
        if (branch == null) {
            throw new Exception("The current manager is not assigned to a branch");
        }

        return shiftReportRepository
                .findActiveCashierShiftsByBranch(branch.getId(), UserRole.ROLE_BRANCH_CASHIER)
                .stream()
                .map(this::buildActiveShiftProgress)
                .collect(Collectors.toList());
    }


    private List<PaymentSummary> getPaymentSummaries(List<Order> orders, BigDecimal totalSales) {
        Map<PaymentType, List<Order>> groupedOrders = orders.stream()
                    .collect(Collectors.groupingBy(order -> order.getPaymentType() != null ? order.getPaymentType() : PaymentType.CASH));


        List<PaymentSummary> paymentSummaries = new ArrayList<>();

        for (Map.Entry<PaymentType, List<Order>> entry: groupedOrders.entrySet()){
            BigDecimal amount = entry.getValue().stream()
                    .map(Order::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int transactions = entry.getValue().size();

            double percent = totalSales.signum() == 0 ? 0.0
                    : amount.multiply(BigDecimal.valueOf(100))
                    .divide(totalSales, 4, RoundingMode.HALF_UP)
                    .doubleValue();

            PaymentSummary ps = new PaymentSummary();

            ps.setType(entry.getKey());
            ps.setTotalAmount(amount);
            ps.setTransactionCount(transactions);
            ps.setPercentage(percent);
            paymentSummaries.add(ps);

        }
        return paymentSummaries;
    }

    private List<Product> getTopSellingProducts(List<Order> orders) {
        Map<Product, Long> productSalesMap = new HashMap<>();

        for (Order order : orders){
            for(OrderItem item : order.getItems()){
                Product product = item.getProduct();
                productSalesMap.put(product, productSalesMap.getOrDefault(product, 0L) + item.getQuantity());
            }
        }
        return productSalesMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<Order> getRecentOrders(List<Order> orders) {
            return orders.stream()
                    .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                    .limit(5)
                    .collect(Collectors.toList());
    }

    private ShiftReportDTO buildActiveShiftProgress(ShiftReport shiftReport) {
        User cashier = shiftReport.getCashier();
        LocalDateTime now = LocalDateTime.now();

        List<Order> orders = orderRepository.findByCashierAndCreatedAtBetween(
                cashier, shiftReport.getShiftStart(), now);

        List<Refund> refunds = findRefundsForShift(shiftReport, now);

        BigDecimal totalRefunds = refunds.stream()
                .map(Refund::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSales = orders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        shiftReport.setTotalRefunds(totalRefunds);
        shiftReport.setTotalSales(totalSales);
        shiftReport.setTotalOrders((double) orders.size());
        shiftReport.setNetSales(totalSales.subtract(totalRefunds));
        shiftReport.setRecentOrders(getRecentOrders(orders));
        shiftReport.setTopSellingProducts(getTopSellingProducts(orders));
        shiftReport.setPaymentSummaries(getPaymentSummaries(orders, totalSales));
        shiftReport.setRefunds(refunds);

        return ShiftReportMapper.toDTO(shiftReportRepository.save(shiftReport));
    }

    private ShiftReport getActiveShiftForUpdate(User cashier) {
        return shiftReportRepository.findActiveByCashierForUpdate(cashier, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "There is no active shift to end"));
    }

    private List<Refund> findRefundsForShift(ShiftReport shiftReport, LocalDateTime end) {
        List<Refund> refunds = new ArrayList<>(
                refundRepository.findByShiftReportId(shiftReport.getId()));
        refundRepository.findByCashierIdAndCreatedAtBetween(
                        shiftReport.getCashier().getId(), shiftReport.getShiftStart(), end)
                .stream()
                .filter(refund -> refund.getShiftReport() == null)
                .forEach(refunds::add);
        return refunds;
    }
}
