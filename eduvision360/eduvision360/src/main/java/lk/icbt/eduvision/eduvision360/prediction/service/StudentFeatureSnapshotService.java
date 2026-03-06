package lk.icbt.eduvision.eduvision360.prediction.service;

import lk.icbt.eduvision.eduvision360.prediction.model.StudentFeatureSnapshot;

import java.util.Map;

public interface StudentFeatureSnapshotService {

    StudentFeatureSnapshot buildAndSaveSnapshot(
            String studentId,
            String courseId,
            Integer windowDays,
            Map<String, Object> overrides
    );
}