package lk.icbt.eduvision.eduvision360.admin.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartItemDto {
    private String label;
    private long value;
}