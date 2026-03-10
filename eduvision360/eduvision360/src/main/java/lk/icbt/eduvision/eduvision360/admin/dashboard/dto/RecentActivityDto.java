package lk.icbt.eduvision.eduvision360.admin.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentActivityDto {
    private String type;
    private String title;
    private String subtitle;
    private Instant timestamp;
}