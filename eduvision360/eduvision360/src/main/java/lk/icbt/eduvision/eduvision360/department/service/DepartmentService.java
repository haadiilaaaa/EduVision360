package lk.icbt.eduvision.eduvision360.department.service;

import lk.icbt.eduvision.eduvision360.department.dto.CreateDepartmentRequest;
import lk.icbt.eduvision.eduvision360.department.dto.DepartmentResponse;
import lk.icbt.eduvision.eduvision360.department.dto.UpdateDepartmentRequest;

import java.util.List;

public interface DepartmentService {

    DepartmentResponse create(CreateDepartmentRequest req);

    List<DepartmentResponse> getAll();

    DepartmentResponse getById(String id);

    DepartmentResponse update(String id, UpdateDepartmentRequest req);

    void delete(String id);
}