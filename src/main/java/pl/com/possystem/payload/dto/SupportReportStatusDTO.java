package pl.com.possystem.payload.dto;

import lombok.Data;
import pl.com.possystem.domain.SupportReportStatus;

@Data
public class SupportReportStatusDTO {
    private SupportReportStatus status;
}
