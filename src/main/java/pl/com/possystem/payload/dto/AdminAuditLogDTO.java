package pl.com.possystem.payload.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminAuditLogDTO {
    private Long id;
    private Long actorId;
    private String actorEmail;
    private String action;
    private String resourceType;
    private String resourceId;
    private String details;
    private LocalDateTime createdAt;
}
