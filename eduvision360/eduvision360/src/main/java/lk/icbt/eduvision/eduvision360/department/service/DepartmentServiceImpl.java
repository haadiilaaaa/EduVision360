package lk.icbt.eduvision.eduvision360.department.service;

import lk.icbt.eduvision.eduvision360.department.dto.CreateDepartmentRequest;
import lk.icbt.eduvision.eduvision360.department.dto.DepartmentResponse;
import lk.icbt.eduvision.eduvision360.department.dto.UpdateDepartmentRequest;
import lk.icbt.eduvision.eduvision360.department.model.Department;
import lk.icbt.eduvision.eduvision360.department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public DepartmentResponse create(CreateDepartmentRequest req) {
        String code = req.getCode().trim().toUpperCase();
        String name = req.getName().trim();

        if (departmentRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Department code already exists");
        }

        if (departmentRepository.existsByName(name)) {
            throw new IllegalArgumentException("Department name already exists");
        }

        Department department = Department.builder()
                .code(code)
                .name(name)
                .description(req.getDescription() != null ? req.getDescription().trim() : null)
                .createdAt(Instant.now())
                .build();

        return toResponse(departmentRepository.save(department));
    }

    @Override
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Department::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Override
    public DepartmentResponse getById(String id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        return toResponse(department);
    }

    @Override
    public DepartmentResponse update(String id, UpdateDepartmentRequest req) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        String code = req.getCode().trim().toUpperCase();
        String name = req.getName().trim();

        if (!department.getCode().equals(code) && departmentRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Department code already exists");
        }

        if (!department.getName().equals(name) && departmentRepository.existsByName(name)) {
            throw new IllegalArgumentException("Department name already exists");
        }

        department.setCode(code);
        department.setName(name);
        department.setDescription(req.getDescription() != null ? req.getDescription().trim() : null);

        return toResponse(departmentRepository.save(department));
    }

    @Override
    public void delete(String id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        departmentRepository.delete(department);
    }

    private DepartmentResponse toResponse(Department d) {
        return new DepartmentResponse(
                d.getId(),
                d.getCode(),
                d.getName(),
                d.getDescription(),
                d.getCreatedAt()
        );
    }
}