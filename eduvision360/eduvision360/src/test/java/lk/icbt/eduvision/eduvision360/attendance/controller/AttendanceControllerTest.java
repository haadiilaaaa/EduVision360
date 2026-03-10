package lk.icbt.eduvision.eduvision360.attendance.controller;

import lk.icbt.eduvision.eduvision360.attendance.dto.AttendanceMarkResponse;
import lk.icbt.eduvision.eduvision360.attendance.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Batch 5B - Attendance Controller Automated Tests")
class AttendanceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private AttendanceController attendanceController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(attendanceController)
                .build();
    }

    @Test
    @Order(34)
    @DisplayName("ATC-34: Student should be able to mark attendance with valid input")
    void markAttendance_shouldSucceed_withValidInput() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "face.jpg",
                "image/jpeg",
                "dummy-image-content".getBytes()
        );

        AttendanceMarkResponse response = AttendanceMarkResponse.builder()
                .success(true)
                .status("PRESENT")
                .message("Attendance marked successfully")
                .confidence(0.98)
                .classId("SE101")
                .date("2026-03-08")
                .time("10:30:00")
                .build();

        when(attendanceService.markAttendance(any(), eq("session-001"), any()))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/attendance/mark")
                        .file(image)
                        .param("sessionId", "session-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value("PRESENT"))
                .andExpect(jsonPath("$.message").value("Attendance marked successfully"));
    }

    @Test
    @Order(35)
    @DisplayName("ATC-35: Duplicate attendance marking should be rejected")
    void markAttendance_shouldRejectDuplicateAttendance() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "face.jpg",
                "image/jpeg",
                "dummy-image-content".getBytes()
        );

        doThrow(new IllegalStateException("Attendance already marked for this session"))
                .when(attendanceService).markAttendance(any(), eq("session-001"), any());

        mockMvc.perform(multipart("/api/attendance/mark")
                        .file(image)
                        .param("sessionId", "session-001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Attendance already marked for this session"));
    }

    @Test
    @Order(36)
    @DisplayName("ATC-36: Attendance marking should fail when the session is invalid")
    void markAttendance_shouldFail_whenSessionIsInvalid() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "face.jpg",
                "image/jpeg",
                "dummy-image-content".getBytes()
        );

        doThrow(new IllegalArgumentException("Class session not found"))
                .when(attendanceService).markAttendance(any(), eq("invalid-session"), any());

        mockMvc.perform(multipart("/api/attendance/mark")
                        .file(image)
                        .param("sessionId", "invalid-session"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Class session not found"));
    }
}