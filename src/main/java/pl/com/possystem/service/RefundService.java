package pl.com.possystem.service;

import org.springframework.cglib.core.Local;
import pl.com.possystem.entity.Refund;
import pl.com.possystem.payload.dto.RefundDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface RefundService {

    RefundDTO createRefund(RefundDTO refund) throws Exception;

    List<RefundDTO> getAllRefunds() throws Exception;

    List<RefundDTO> getRefundByCashier(Long cashierId) throws Exception;

    List<RefundDTO> getRefundByShiftReport(Long shiftReportId) throws Exception;

    List<RefundDTO> getRefundIdByCashierAndDateRange(Long cashierId, LocalDateTime startDate, LocalDateTime endDate) throws Exception;

    List<RefundDTO> getRefundByBranch (Long branchId) throws Exception;

    RefundDTO getRefundById(Long refundId) throws Exception;

    void deleteRefund(Long refundId) throws Exception;






}
