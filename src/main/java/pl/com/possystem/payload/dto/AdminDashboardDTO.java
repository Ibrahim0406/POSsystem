package pl.com.possystem.payload.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AdminDashboardDTO {
    private long stores;
    private Map<String, Long> storesByStatus;
    private long users;
    private Map<String, Long> usersByRole;
    private long openSupportReports;
}
