package lk.icbt.eduvision.eduvision360.analytics.controller;

import lk.icbt.eduvision.eduvision360.analytics.dto.BusinessAnalyticsResponse;
import lk.icbt.eduvision.eduvision360.analytics.service.BusinessAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AdminBusinessAnalyticsController {

    private final BusinessAnalyticsService businessAnalyticsService;

    @GetMapping("/business")
    @PreAuthorize("hasAuthority('ADMIN')")
    public BusinessAnalyticsResponse business() {
        return businessAnalyticsService.getBusinessAnalytics();
    }
}