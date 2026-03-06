package lk.icbt.eduvision.eduvision360.notification.model;

public enum NotificationType {

    // Existing (DON'T RENAME)
    COURSE_ENROLLED,
    NEW_STUDENT_ENROLLED,
    CLASS_SESSION_CREATED,
    CLASS_SESSION_OPENED,
    CLASS_SESSION_COMPLETED,
    MATERIAL_UPLOADED,
    ANNOUNCEMENT_POSTED,
    SYSTEM_ALERT,

    // Added for your AI + attendance flows
    DROPOUT_HIGH_RISK,
    DROPOUT_MEDIUM_RISK,
    LOW_ENGAGEMENT,
    ATTENDANCE_MARKED,
    ATTENDANCE_MISSED,

    // Optional session automation UX
    CLASS_SESSION_CLOSING_SOON,
    CLASS_SESSION_CANCELLED
}