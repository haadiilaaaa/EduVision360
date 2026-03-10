package lk.icbt.eduvision.eduvision360.announcement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.icbt.eduvision.eduvision360.announcement.dto.AnnouncementResponse;
import lk.icbt.eduvision.eduvision360.announcement.service.AnnouncementService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Batch 6C - Announcement Controller Automated Tests")
class AnnouncementControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AnnouncementService announcementService;

    @InjectMocks
    private AnnouncementController announcementController;

    private Authentication auth;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders
                .standaloneSetup(announcementController)
                .build();

        auth = new UsernamePasswordAuthenticationToken("student1", null);
    }

    @Test
    @Order(42)
    @DisplayName("ATC-42: Student should be able to retrieve announcements successfully")
    void getStudentAnnouncements_shouldReturnAnnouncements() throws Exception {
        List<AnnouncementResponse> announcements = List.of(
                new AnnouncementResponse(
                        "a1",
                        "course1",
                        "SE101",
                        "Software Engineering",
                        "teacher1",
                        "Teacher One",
                        "New Announcement",
                        "Please submit your work by Friday.",
                        Instant.now(),
                        null
                )
        );

        when(announcementService.getStudentAnnouncementsForMyCourses(eq("student1")))
                .thenReturn(announcements);

        mockMvc.perform(get("/api/student/announcements/my-courses").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("a1"))
                .andExpect(jsonPath("$[0].courseCode").value("SE101"))
                .andExpect(jsonPath("$[0].title").value("New Announcement"));
    }
}