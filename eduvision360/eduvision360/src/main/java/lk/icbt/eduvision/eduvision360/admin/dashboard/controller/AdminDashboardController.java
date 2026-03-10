package lk.icbt.eduvision.eduvision360.admin.dashboard.controller;

import lk.icbt.eduvision.eduvision360.admin.dashboard.dto.AdminDashboardSummaryResponse;
import lk.icbt.eduvision.eduvision360.admin.dashboard.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ADMIN')")
    public AdminDashboardSummaryResponse getSummary() {
        return adminDashboardService.getSummary();
    }
}