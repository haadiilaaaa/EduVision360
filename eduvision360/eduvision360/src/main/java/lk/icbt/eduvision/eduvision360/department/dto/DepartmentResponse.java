package lk.icbt.eduvision.eduvision360.department.dto;

import java.time.Instant;

public record DepartmentResponse(
        String id,
        String code,
        String name,
        String description,
        Instant createdAt
) {}