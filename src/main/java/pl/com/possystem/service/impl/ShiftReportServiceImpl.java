package pl.com.possystem.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import pl.com.possystem.domain.UserRole;
import org.springframework.stereotype.Service;
import pl.com.possystem.domain.PaymentType;
import pl.com.possystem.entity.*;
import pl.com.possystem.mapper.ShiftReportMapper;
import pl.com.possystem.payload.dto.ShiftReportDTO;
import pl.com.possystem.repository.*;
import pl.com.possystem.service.ShiftReportService;
import pl.com.possystem.service.UserService;

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

    @Override
    public ShiftReportDTO startShift() throws Exception {

        User currentUser = userService.getCurrentUser();
        Branch branch = currentUser.getBranch();
        if (branch == null) {
            throw new Exception("The current user is not assigned to a branch and cannot start a shift");
        }

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
    public ShiftReportDTO endShift(Long shiftReportId, LocalDateTime shiftEnd) throws Exception {

        User user = userService.getCurrentUser();

        ShiftReport shiftReport = shiftReportRepository.findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(user)
                .orElseThrow(() -> new Exception("Shift report not found"));

        shiftReport.setShiftEnd(shiftEnd);

        List<Refund> refunds = refundRepository.findByCashierIdAndCreatedAtBetween(
                user.getId() ,shiftReport.getShiftStart(), shiftReport.getShiftEnd()
        );

        double totalRefunds = refunds.stream().mapToDouble(refund -> refund.getAmount() != null? refund.getAmount() : 0.0).sum();

        List<Order> orders = orderRepository.findByCashierAndCreatedAtBetween(
                user, shiftReport.getShiftStart(), shiftReport.getShiftEnd()
        );

        double totalSales = orders.stream().mapToDouble(Order::getTotalAmount).sum();

        double totalOrders = orders.size();

        double netSales = totalSales - totalRefunds;


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
        return shiftReportRepository.findById(id).map(ShiftReportMapper ::toDTO).orElseThrow(() -> new Exception("Shift report not found"));
    }

    @Override
    public List<ShiftReportDTO> getAllShiftReports() {
        List<ShiftReport> shiftReports = shiftReportRepository.findAll();
        return shiftReports.stream().map(ShiftReportMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ShiftReportDTO> getShiftReportsByBranchId(Long branchId) {
        List<ShiftReport> shiftReports = shiftReportRepository.findByBranchId(branchId);
        return shiftReports.stream().map(ShiftReportMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ShiftReportDTO> getShiftReportsByCashierId(Long cashierId) {
        List<ShiftReport> shiftReports = shiftReportRepository.findByCashierId(cashierId);
        return shiftReports.stream().map(ShiftReportMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public ShiftReportDTO getCurrentShiftProgress(Long cashierId) throws Exception {

        User cashier = userService.getCurrentUser();

        ShiftReport shiftReport = shiftReportRepository
                .findTopByCashierAndShiftEndIsNullOrderByShiftStartDesc(cashier)
                .orElseThrow(() -> new Exception("No active shift found for the cashier"));

        return buildActiveShiftProgress(shiftReport);
    }

    @Override
    public ShiftReportDTO getShiftByCashierAndDate(Long cashierId, LocalDateTime date) throws Exception {

        User cashier = userRepository.findById(cashierId).orElseThrow(() -> new Exception("Cashier not found"));

        LocalDateTime start = date.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = date.withHour(23).withMinute(59).withSecond(59);

        ShiftReport report = shiftReportRepository.findByCashierAndShiftStartBetween(cashier, start, end)
                .orElseThrow(() -> new Exception("No shift report found for the given cashier and date"));

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


    private List<PaymentSummary> getPaymentSummaries(List<Order> orders, double totalSales) {
        Map<PaymentType, List<Order>> groupedOrders = orders.stream()
                    .collect(Collectors.groupingBy(order -> order.getPaymentType() != null ? order.getPaymentType() : PaymentType.CASH));


        List<PaymentSummary> paymentSummaries = new ArrayList<>();

        for (Map.Entry<PaymentType, List<Order>> entry: groupedOrders.entrySet()){
            double amount = entry.getValue().stream().mapToDouble(Order::getTotalAmount).sum();
            int transactions = entry.getValue().size();

            double percent = (amount/totalSales) * 100;

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

        List<Refund> refunds = refundRepository.findByCashierIdAndCreatedAtBetween(
                cashier.getId(), shiftReport.getShiftStart(), now);

        double totalRefunds = refunds.stream()
                .mapToDouble(refund -> refund.getAmount() != null ? refund.getAmount() : 0.0)
                .sum();
        double totalSales = orders.stream().mapToDouble(Order::getTotalAmount).sum();

        shiftReport.setTotalRefunds(totalRefunds);
        shiftReport.setTotalSales(totalSales);
        shiftReport.setTotalOrders((double) orders.size());
        shiftReport.setNetSales(totalSales - totalRefunds);
        shiftReport.setRecentOrders(getRecentOrders(orders));
        shiftReport.setTopSellingProducts(getTopSellingProducts(orders));
        shiftReport.setPaymentSummaries(getPaymentSummaries(orders, totalSales));
        shiftReport.setRefunds(refunds);

        return ShiftReportMapper.toDTO(shiftReportRepository.save(shiftReport));
    }
}
