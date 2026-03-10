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
public class AdminDashboardSummaryResponse {
    private AdminOverviewDto overview;
    private AdminChartsDto charts;
    private List<RecentActivityDto> recentActivities;
}