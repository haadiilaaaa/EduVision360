import axios from "axios";

// Optional: use env when deployed (keep localhost as fallback)
const BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080/api";

function authHeaders() {
  const token = localStorage.getItem("token");

  // ✅ IMPORTANT: don't send Bearer null
  if (!token) return {};

  return {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };
}

/* =========================
   Departments
========================= */
export const getDepartments = () => axios.get(`${BASE_URL}/departments`, authHeaders());
export const createDepartment = (data) => axios.post(`${BASE_URL}/departments`, data, authHeaders());
export const updateDepartment = (id, data) => axios.put(`${BASE_URL}/departments/${id}`, data, authHeaders());
export const deleteDepartment = (id) => axios.delete(`${BASE_URL}/departments/${id}`, authHeaders());

/* =========================
   Courses
========================= */
export const getCourses = () => axios.get(`${BASE_URL}/courses`, authHeaders());
export const createCourse = (data) => axios.post(`${BASE_URL}/courses`, data, authHeaders());
export const updateCourse = (id, data) => axios.put(`${BASE_URL}/courses/${id}`, data, authHeaders());
export const deleteCourse = (id) => axios.delete(`${BASE_URL}/courses/${id}`, authHeaders());

export const getMyTeacherCourses = () => axios.get(`${BASE_URL}/courses/teacher/my`, authHeaders());

export const getTeacherAttendance = (params) =>
  axios.get(`${BASE_URL}/teacher/attendance`, { ...authHeaders(), params });

export const enrollCourse = (courseId) =>
  axios.post(`${BASE_URL}/student/enrollments`, { courseId }, authHeaders());

export const getMyEnrollments = () =>
  axios.get(`${BASE_URL}/student/enrollments/my`, authHeaders());

/* =========================
   Learning Materials
========================= */
export const createMaterial = (data) =>
  axios.post(`${BASE_URL}/teacher/materials`, data, authHeaders());

export const getMyTeacherMaterials = () =>
  axios.get(`${BASE_URL}/teacher/materials/my`, authHeaders());

export const getTeacherMaterialsByCourse = (courseId) =>
  axios.get(`${BASE_URL}/teacher/materials/course/${courseId}`, authHeaders());

export const deleteTeacherMaterial = (materialId) =>
  axios.delete(`${BASE_URL}/teacher/materials/${materialId}`, authHeaders());

export const getStudentMaterials = () =>
  axios.get(`${BASE_URL}/student/materials/my-courses`, authHeaders());

export const getStudentMaterialsByCourse = (courseId) =>
  axios.get(`${BASE_URL}/student/materials/course/${courseId}`, authHeaders());

export const updateTeacherMaterial = (materialId, data) =>
  axios.put(`${BASE_URL}/teacher/materials/${materialId}`, data, authHeaders());

export const trackMaterialView = (materialId) =>
  axios.post(`${BASE_URL}/student/materials/${materialId}/view`, {}, authHeaders());

/* =========================
   Announcements
========================= */
export const createAnnouncement = (data) =>
  axios.post(`${BASE_URL}/teacher/announcements`, data, authHeaders());

export const updateTeacherAnnouncement = (announcementId, data) =>
  axios.put(`${BASE_URL}/teacher/announcements/${announcementId}`, data, authHeaders());

export const getMyTeacherAnnouncements = () =>
  axios.get(`${BASE_URL}/teacher/announcements/my`, authHeaders());

export const getTeacherAnnouncementsByCourse = (courseId) =>
  axios.get(`${BASE_URL}/teacher/announcements/course/${courseId}`, authHeaders());

export const deleteTeacherAnnouncement = (announcementId) =>
  axios.delete(`${BASE_URL}/teacher/announcements/${announcementId}`, authHeaders());

export const getStudentAnnouncements = () =>
  axios.get(`${BASE_URL}/student/announcements/my-courses`, authHeaders());

export const getStudentAnnouncementsByCourse = (courseId) =>
  axios.get(`${BASE_URL}/student/announcements/course/${courseId}`, authHeaders());

/* =========================
   Profile
========================= */
export const getMyProfile = () => axios.get(`${BASE_URL}/profile/me`, authHeaders());
export const updateMyProfile = (data) => axios.put(`${BASE_URL}/profile/me`, data, authHeaders());

export const getStudentSessions = () => axios.get(`${BASE_URL}/class-sessions/student/my`, authHeaders());
export const getMyAttendanceHistory = () => axios.get(`${BASE_URL}/attendance/my`, authHeaders());

/* =========================
   Engagement / Emotion Analytics
========================= */
export const getTeacherEngagementSummary = (courseId) =>
  axios.get(`${BASE_URL}/engagement/teacher/summary`, {
    ...authHeaders(),
    params: { courseId },
  });

export const getAdminEngagementSummary = () =>
  axios.get(`${BASE_URL}/engagement/admin/summary`, authHeaders());

/* =========================
   Class Sessions
========================= */
export const getTeacherSessions = () => axios.get(`${BASE_URL}/class-sessions/teacher/my`, authHeaders());
export const createClassSession = (data) => axios.post(`${BASE_URL}/class-sessions`, data, authHeaders());
export const updateClassSessionStatus = (sessionId, status) =>
  axios.put(`${BASE_URL}/class-sessions/${sessionId}/status`, { status }, authHeaders());

/* =========================
   Notifications
========================= */
export const getMyNotifications = (limit = 30) =>
  axios.get(`${BASE_URL}/notifications/my`, { ...authHeaders(), params: { limit } });

export const getMyUnreadNotifications = (limit = 30) =>
  axios.get(`${BASE_URL}/notifications/my/unread`, { ...authHeaders(), params: { limit } });

export const getMyUnreadCount = () =>
  axios.get(`${BASE_URL}/notifications/my/unread-count`, authHeaders());

export const markNotificationAsRead = (notificationId) =>
  axios.put(`${BASE_URL}/notifications/${notificationId}/read`, {}, authHeaders());

export const markAllNotificationsAsRead = () =>
  axios.put(`${BASE_URL}/notifications/my/read-all`, {}, authHeaders());

/* =========================
   Feedback
========================= */
export const submitFeedback = (data) => axios.post(`${BASE_URL}/student/feedback`, data, authHeaders());
export const getMyFeedback = () => axios.get(`${BASE_URL}/student/feedback/my`, authHeaders());
export const getTeacherFeedback = () => axios.get(`${BASE_URL}/teacher/feedback/my-courses`, authHeaders());
export const updateTeacherFeedbackStatus = (feedbackId, data) =>
  axios.put(`${BASE_URL}/teacher/feedback/${feedbackId}/status`, data, authHeaders());
export const getAdminFeedback = () => axios.get(`${BASE_URL}/admin/feedback`, authHeaders());
export const updateAdminFeedbackStatus = (feedbackId, data) =>
  axios.put(`${BASE_URL}/admin/feedback/${feedbackId}/status`, data, authHeaders());

/* =========================
   AI Tutor / Chatbot
========================= */
export const askAiChatbot = (data) => axios.post(`${BASE_URL}/student/ai/chatbot`, data, authHeaders());
export const askAiTutor = (data) => axios.post(`${BASE_URL}/student/ai/tutor`, data, authHeaders());
export const generateAiSummary = (data) => axios.post(`${BASE_URL}/student/ai/summary`, data, authHeaders());
export const generateAiQuiz = (data) => axios.post(`${BASE_URL}/student/ai/quiz`, data, authHeaders());
export const getMyAiHistory = () => axios.get(`${BASE_URL}/student/ai/history`, authHeaders());

export const sendTeacherMessage = (data) =>
  axios.post(`${BASE_URL}/teacher/messages`, data, authHeaders());

export const getTeacherSentMessages = () =>
  axios.get(`${BASE_URL}/teacher/messages/sent`, authHeaders());

export const getTeacherMessageStudents = () =>
  axios.get(`${BASE_URL}/teacher/messages/students`, authHeaders());

export const getStudentInboxMessages = () =>
  axios.get(`${BASE_URL}/student/messages/inbox`, authHeaders());

export const markStudentMessageAsRead = (messageId) =>
  axios.put(`${BASE_URL}/student/messages/${messageId}/read`, {}, authHeaders());

export const generateTeacherQuizDraft = (data) =>
  axios.post(`${BASE_URL}/teacher/quizzes/generate`, data, authHeaders());

export const saveTeacherQuiz = (data) =>
  axios.post(`${BASE_URL}/teacher/quizzes`, data, authHeaders());

export const getMyTeacherQuizzes = () =>
  axios.get(`${BASE_URL}/teacher/quizzes`, authHeaders());

export const getTeacherQuizzesByCourse = (courseId) =>
  axios.get(`${BASE_URL}/teacher/quizzes/course/${courseId}`, authHeaders());

export const getTeacherQuizById = (quizId) =>
  axios.get(`${BASE_URL}/teacher/quizzes/${quizId}`, authHeaders());

export const getAdminDashboardSummary = () =>
  axios.get(`${BASE_URL}/admin/dashboard/summary`, authHeaders());

export const getInstitutionSettings = () =>
  axios.get(`${BASE_URL}/admin/institution-settings`, authHeaders());

export const updateInstitutionSettings = (data) =>
  axios.put(`${BASE_URL}/admin/institution-settings`, data, authHeaders());