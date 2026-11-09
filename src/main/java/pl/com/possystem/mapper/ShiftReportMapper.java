package pl.com.possystem.mapper;


import pl.com.possystem.entity.Order;
import pl.com.possystem.entity.Product;
import pl.com.possystem.entity.Refund;
import pl.com.possystem.entity.ShiftReport;
import pl.com.possystem.payload.dto.OrderDTO;
import pl.com.possystem.payload.dto.ProductDTO;
import pl.com.possystem.payload.dto.RefundDTO;
import pl.com.possystem.payload.dto.ShiftReportDTO;

import java.util.List;
import java.util.stream.Collectors;

public class ShiftReportMapper {

public static ShiftReportDTO toDTO(ShiftReport shiftReport){

    return ShiftReportDTO.builder()
            .id(shiftReport.getId())
            .shiftEnd(shiftReport.getShiftEnd())
            .shiftStart(shiftReport.getShiftStart())
            .totalSales(shiftReport.getTotalSales())
            .totalRefunds(shiftReport.getTotalRefunds())
            .netSales(shiftReport.getNetSales())
            .totalOrders(shiftReport.getTotalOrders())
            .cashier(UserMapper.toDTO(shiftReport.getCashier()))
            .cashierId(shiftReport.getCashier() != null ? shiftReport.getCashier().getId() : null)
            .branch(shiftReport.getBranch() != null ? BranchMapper.toDTO(shiftReport.getBranch()) : null)
            .branchId(shiftReport.getBranch() != null ? shiftReport.getBranch().getId() : null)
            .recentOrders(mapOrders(shiftReport.getRecentOrders()))
            .topSellingProducts(mapProducts(shiftReport.getTopSellingProducts()))
            .refunds(mapRefunds(shiftReport.getRefunds()))
            .paymentSummaries(shiftReport.getPaymentSummaries())
            .build();
}

    private static List<RefundDTO> mapRefunds(List<Refund> refunds) {
        if(refunds == null || refunds.isEmpty()) {
            return null;
        }
            return refunds.stream().map(RefundMapper::toDTO).collect(Collectors.toList());
        }


    private static List<ProductDTO> mapProducts(List<Product> topSellingProducts) {
        if(topSellingProducts == null || topSellingProducts.isEmpty()) {
            return null;
        }
        return topSellingProducts.stream().map(ProductMapper::toDTO).collect(Collectors.toList());
    }

    private static List<OrderDTO> mapOrders(List<Order> recentOrders) {
        if(recentOrders == null || recentOrders.isEmpty()) {
            return null;
        }
        return recentOrders.stream().map(OrderMapper::toDTO).collect(Collectors.toList());
    }

}
