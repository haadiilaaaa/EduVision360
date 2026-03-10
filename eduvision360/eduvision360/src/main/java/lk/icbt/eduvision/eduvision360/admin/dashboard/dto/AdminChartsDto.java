package lk.icbt.eduvision.eduvision360.admin.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminChartsDto {
    private List<ChartItemDto> usersByRole;
    private List<ChartItemDto> contentActivity;
    private List<ChartItemDto> riskDistribution;
    private List<ChartItemDto> sessionStatusDistribution;
    private List<ChartItemDto> coursesByDepartment;
}