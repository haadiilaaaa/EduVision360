package lk.icbt.eduvision.eduvision360.material.controller;

import jakarta.validation.Valid;
import lk.icbt.eduvision.eduvision360.material.dto.CreateMaterialRequest;
import lk.icbt.eduvision.eduvision360.material.dto.MaterialResponse;
import lk.icbt.eduvision.eduvision360.material.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    // =========================
    // Teacher endpoints
    // =========================

    @PostMapping("/api/teacher/materials")
    public ResponseEntity<MaterialResponse> create(
            @Valid @RequestBody CreateMaterialRequest req,
            Authentication authentication
    ) {
        return ResponseEntity.status(201)
                .body(materialService.create(req, authentication.getName()));
    }

    @GetMapping("/api/teacher/materials/my")
    public ResponseEntity<List<MaterialResponse>> getMyMaterials(Authentication authentication) {
        return ResponseEntity.ok(materialService.getTeacherMaterials(authentication.getName()));
    }

    @GetMapping("/api/teacher/materials/course/{courseId}")
    public ResponseEntity<List<MaterialResponse>> getMyMaterialsByCourse(
            @PathVariable String courseId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                materialService.getTeacherMaterialsByCourse(authentication.getName(), courseId)
        );
    }

    @DeleteMapping("/api/teacher/materials/{materialId}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable String materialId,
            Authentication authentication
    ) {
        materialService.deleteTeacherMaterial(authentication.getName(), materialId);
        return ResponseEntity.ok(Map.of("message", "Material deleted successfully"));
    }

    @PutMapping("/api/teacher/materials/{materialId}")
    public ResponseEntity<MaterialResponse> update(
            @PathVariable String materialId,
            @Valid @RequestBody CreateMaterialRequest req,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                materialService.updateTeacherMaterial(authentication.getName(), materialId, req)
        );
    }

    // =========================
    // Student endpoints
    // =========================

    @GetMapping("/api/student/materials/course/{courseId}")
    public ResponseEntity<List<MaterialResponse>> getStudentMaterialsByCourse(
            @PathVariable String courseId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                materialService.getStudentMaterialsForCourse(authentication.getName(), courseId)
        );
    }

    @GetMapping("/api/student/materials/my-courses")
    public ResponseEntity<List<MaterialResponse>> getStudentMaterials(Authentication authentication) {
        return ResponseEntity.ok(
                materialService.getStudentMaterialsForMyCourses(authentication.getName())
        );
    }

    @PostMapping("/api/student/materials/{materialId}/view")
    public ResponseEntity<Map<String, String>> trackStudentMaterialView(
            @PathVariable String materialId,
            Authentication authentication
    ) {
        materialService.trackStudentMaterialView(authentication.getName(), materialId);
        return ResponseEntity.ok(Map.of("message", "Material view tracked successfully"));
    }
}