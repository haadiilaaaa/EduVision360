export const notificationTypeMap = {
  COURSE_ENROLLED: { label: "Course Enrolled", fallbackUrl: "/student/my-courses" },
  NEW_STUDENT_ENROLLED: { label: "New Student Enrolled", fallbackUrl: "/teacher/courses" },

  CLASS_SESSION_CREATED: { label: "Session Scheduled", fallbackUrl: "/student" },
  CLASS_SESSION_OPENED: { label: "Attendance Open", fallbackUrl: "/student" },
  CLASS_SESSION_COMPLETED: { label: "Session Completed", fallbackUrl: "/student/attendance-history" },
  CLASS_SESSION_CLOSING_SOON: { label: "Session Closing Soon", fallbackUrl: "/teacher/sessions" },
  CLASS_SESSION_CANCELLED: { label: "Session Cancelled", fallbackUrl: "/teacher/sessions" },

  MATERIAL_UPLOADED: { label: "New Material", fallbackUrl: "/student/materials" },
  ANNOUNCEMENT_POSTED: { label: "New Announcement", fallbackUrl: "/student/announcements" },

  SYSTEM_ALERT: { label: "System Alert", fallbackUrl: "/student" },

  DROPOUT_HIGH_RISK: { label: "High Risk Alert", fallbackUrl: "/teacher" },   // you don’t have /teacher/predictions route
  DROPOUT_MEDIUM_RISK: { label: "Risk Update", fallbackUrl: "/teacher" },

  LOW_ENGAGEMENT: { label: "Low Engagement", fallbackUrl: "/teacher" },

  ATTENDANCE_MARKED: { label: "Attendance Marked", fallbackUrl: "/student/attendance-history" },
  ATTENDANCE_MISSED: { label: "Attendance Missing", fallbackUrl: "/student/attendance-history" },
};