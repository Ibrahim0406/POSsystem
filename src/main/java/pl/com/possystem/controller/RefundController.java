package pl.com.possystem.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.possystem.payload.dto.RefundDTO;
import pl.com.possystem.payload.response.ApiResponse;
import pl.com.possystem.service.RefundService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/create")
    public ResponseEntity<RefundDTO> createRefund(@RequestBody RefundDTO refundDTO) throws Exception {
        RefundDTO createdRefund = refundService.createRefund(refundDTO);
        return ResponseEntity.ok(createdRefund);
    }

    @GetMapping()
    public ResponseEntity<List<RefundDTO>> getAllRefund() throws Exception {
      List<RefundDTO> refunds = refundService.getAllRefunds();
        return ResponseEntity.ok(refunds);
    }

    @GetMapping("/cashier/{cashierId}")
    public ResponseEntity<List<RefundDTO>> getRefundByCashier(@PathVariable Long cashierId) throws Exception {
        List<RefundDTO> refunds = refundService.getRefundByCashier(cashierId);
        return ResponseEntity.ok(refunds);
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<RefundDTO>> getRefundByBranch(@PathVariable Long branchId) throws Exception {
        List<RefundDTO> refunds = refundService.getRefundByBranch(branchId);
        return ResponseEntity.ok(refunds);
    }


    @GetMapping("/shift-report/{shiftReportId}")
    public ResponseEntity<List<RefundDTO>> getRefundByShiftReport(@PathVariable Long shiftReportId) throws Exception {
        List<RefundDTO> refunds = refundService.getRefundByShiftReport(shiftReportId);
        return ResponseEntity.ok(refunds);
    }

    @GetMapping("/cashier/{cashierId}/date-range")
    public ResponseEntity<List<RefundDTO>> getRefundByCashierAndDateRange(@PathVariable Long cashierId,
                                                                          @RequestParam String startDate,
                                                                          @RequestParam String endDate) throws Exception {
        List<RefundDTO> refunds = refundService.getRefundIdByCashierAndDateRange(cashierId,
                LocalDateTime.parse(startDate),
                LocalDateTime.parse(endDate));
        return ResponseEntity.ok(refunds);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefundDTO> getRefundById(@PathVariable Long id) throws Exception {
        RefundDTO refund = refundService.getRefundById(id);
        return ResponseEntity.ok(refund);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteRefund(@PathVariable Long id) throws Exception {
        refundService.deleteRefund(id);
        ApiResponse response = new ApiResponse();
        response.setMessage("Customer deleted successfully");

        return ResponseEntity.ok(response);
    }



}
