import { BrowserRouter, Routes, Route } from "react-router-dom";
import ManageUsers from "./pages/ManageUsers";
import Register from "./pages/Register";
import TeacherRegister from "./pages/TeacherRegister";
import VerifyOtp from "./pages/VerifyOtp";
import Login from "./pages/Login";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";
import TeacherCourses from "./pages/TeacherCourses";
import StudentAttendance from "./pages/StudentAttendance";
import StudentFaceRegister from "./pages/StudentFaceRegister";
import TeacherAttendanceView from "./pages/TeacherAttendanceView";
import StudentDashboard from "./pages/StudentDashboard";
import TeacherDashboard from "./pages/TeacherDashboard";
import AdminDashboard from "./pages/AdminDashboard";
import StudentAttendanceHistory from "./pages/StudentAttendanceHistory";
import AdminAttendanceView from "./pages/AdminAttendanceView";
import StudentCourses from "./pages/StudentCourses";
import StudentMyCourses from "./pages/StudentMyCourses";
import ProtectedRoute from "./components/ProtectedRoute";
import TeacherMaterials from "./pages/TeacherMaterials";
import StudentMaterials from "./pages/StudentMaterials";
import TeacherAnnouncements from "./pages/TeacherAnnouncements";
import StudentAnnouncements from "./pages/StudentAnnouncements";
import StudentProfile from "./pages/StudentProfile";
import TeacherProfile from "./pages/TeacherProfile";
import AdminProfile from "./pages/AdminProfile";
import { ThemeProvider } from "./context/ThemeContext";
import "./styles/theme.css";
import TeacherSessions from "./pages/TeacherSessions";
import StudentFeedback from "./pages/StudentFeedback";
import TeacherFeedback from "./pages/TeacherFeedback";
import AdminFeedback from "./pages/AdminFeedback";
import StudentAiTutor from "./pages/StudentAiTutor";
import StudentEngagement from "./pages/StudentEngagement";
import TeacherMessages from "./pages/TeacherMessages";
import StudentMessages from "./pages/StudentMessages";
import TeacherQuizGenerator from "./pages/TeacherQuizGenerator";

function App() {
  return (
    <ThemeProvider>
      <BrowserRouter>
        <Routes>
          {/* Public routes */}
          <Route path="/" element={<Register />} />
          <Route path="/apply-teacher" element={<TeacherRegister />} />
          <Route path="/verify-otp" element={<VerifyOtp />} />
          <Route path="/login" element={<Login />} />

          {/* Password reset */}
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />

          {/* Student routes */}
          <Route
            path="/student"
            element={
              <ProtectedRoute allowedRoles={["STUDENT"]}>
                <StudentDashboard />
              </ProtectedRoute>
            }
          >
            <Route path="courses" element={<StudentCourses />} />
            <Route path="my-courses" element={<StudentMyCourses />} />
            <Route path="attendance/:sessionId" element={<StudentAttendance />} />
            <Route path="attendance-history" element={<StudentAttendanceHistory />} />
            <Route path="register-face" element={<StudentFaceRegister />} />
            <Route path="materials" element={<StudentMaterials />} />
            <Route path="announcements" element={<StudentAnnouncements />} />
            <Route path="profile" element={<StudentProfile />} />
            <Route path="feedback" element={<StudentFeedback />} />
            <Route path="ai-tutor" element={<StudentAiTutor />} />
            <Route path="engagement/:sessionId" element={<StudentEngagement />} />
            <Route path="messages" element={<StudentMessages />} />
          </Route>

          {/* Teacher routes */}
          <Route
            path="/teacher"
            element={
              <ProtectedRoute allowedRoles={["TEACHER"]}>
                <TeacherDashboard />
              </ProtectedRoute>
            }
          />

          <Route
            path="/teacher/courses"
            element={
              <ProtectedRoute allowedRoles={["TEACHER"]}>
                <TeacherCourses />
              </ProtectedRoute>
            }
          />

          <Route
            path="/teacher/attendance"
            element={
              <ProtectedRoute allowedRoles={["TEACHER"]}>
                <TeacherAttendanceView />
              </ProtectedRoute>
            }
          />

          <Route
            path="/teacher/materials"
            element={
              <ProtectedRoute allowedRoles={["TEACHER"]}>
                <TeacherMaterials />
              </ProtectedRoute>
            }
          />

          <Route
            path="/teacher/announcements"
            element={
              <ProtectedRoute allowedRoles={["TEACHER"]}>
                <TeacherAnnouncements />
              </ProtectedRoute>
            }
          />

          <Route
            path="/teacher/profile"
            element={
              <ProtectedRoute allowedRoles={["TEACHER"]}>
                <TeacherProfile />
              </ProtectedRoute>
            }
          />

          <Route
            path="/teacher/sessions"
            element={
              <ProtectedRoute allowedRoles={["TEACHER"]}>
                <TeacherSessions />
              </ProtectedRoute>
            }
          />

          <Route
            path="/teacher/feedback"
            element={
              <ProtectedRoute allowedRoles={["TEACHER"]}>
                <TeacherFeedback />
              </ProtectedRoute>
            }
          />

          <Route
            path="/teacher/messages"
            element={
              <ProtectedRoute allowedRoles={["TEACHER"]}>
                <TeacherMessages />
              </ProtectedRoute>
            }
          />

          <Route
            path="/teacher/quizzes"
            element={
              <ProtectedRoute allowedRoles={["TEACHER"]}>
                <TeacherQuizGenerator />
              </ProtectedRoute>
            }
          />

          {/* Admin routes */}
          <Route
            path="/admin"
            element={
              <ProtectedRoute allowedRoles={["ADMIN"]}>
                <AdminDashboard />
              </ProtectedRoute>
            }
          />

          <Route
            path="/admin/attendance"
            element={
              <ProtectedRoute allowedRoles={["ADMIN"]}>
                <AdminAttendanceView />
              </ProtectedRoute>
            }
          />

          <Route
            path="/admin/users"
            element={
              <ProtectedRoute allowedRoles={["ADMIN"]}>
                <ManageUsers />
              </ProtectedRoute>
            }
          />

          <Route
            path="/admin/profile"
            element={
              <ProtectedRoute allowedRoles={["ADMIN"]}>
                <AdminProfile />
              </ProtectedRoute>
            }
          />

          <Route
            path="/admin/feedback"
            element={
              <ProtectedRoute allowedRoles={["ADMIN"]}>
                <AdminFeedback />
              </ProtectedRoute>
            }
          />
        </Routes>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;