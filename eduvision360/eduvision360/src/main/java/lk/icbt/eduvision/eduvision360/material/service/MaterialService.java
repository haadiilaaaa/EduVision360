package lk.icbt.eduvision.eduvision360.material.service;

import lk.icbt.eduvision.eduvision360.material.dto.CreateMaterialRequest;
import lk.icbt.eduvision.eduvision360.material.dto.MaterialResponse;

import java.util.List;

public interface MaterialService {

    MaterialResponse create(CreateMaterialRequest req, String teacherId);

    MaterialResponse updateTeacherMaterial(String teacherId, String materialId, CreateMaterialRequest req);

    List<MaterialResponse> getTeacherMaterials(String teacherId);

    List<MaterialResponse> getTeacherMaterialsByCourse(String teacherId, String courseId);

    void deleteTeacherMaterial(String teacherId, String materialId);

    List<MaterialResponse> getStudentMaterialsForCourse(String studentId, String courseId);

    List<MaterialResponse> getStudentMaterialsForMyCourses(String studentId);

    void trackStudentMaterialView(String studentId, String materialId);
}