package lk.icbt.eduvision.eduvision360.department.controller;

import jakarta.validation.Valid;
import lk.icbt.eduvision.eduvision360.department.dto.CreateDepartmentRequest;
import lk.icbt.eduvision.eduvision360.department.dto.DepartmentResponse;
import lk.icbt.eduvision.eduvision360.department.dto.UpdateDepartmentRequest;
import lk.icbt.eduvision.eduvision360.department.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody CreateDepartmentRequest req) {
        return ResponseEntity.status(201).body(departmentService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAll() {
        return ResponseEntity.ok(departmentService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateDepartmentRequest req
    ) {
        return ResponseEntity.ok(departmentService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String id) {
        departmentService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Department deleted successfully"));
    }
}