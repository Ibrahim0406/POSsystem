package pl.com.possystem.payload.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SystemSettingDTO {
    private String key;
    private String value;
    private LocalDateTime updatedAt;
}
