package pl.com.possystem.payload.dto;

import lombok.Builder;
import lombok.Data;
import pl.com.possystem.domain.SupportReportStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class SupportReportDTO {
    private Long id;
    private String title;
    private String description;
    private SupportReportStatus status;
    private Long submittedById;
    private String submittedByEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
