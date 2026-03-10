package lk.icbt.eduvision.eduvision360.security;

import lk.icbt.eduvision.eduvision360.admin.controller.AdminController;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.auth.service.EmailSender;
import lk.icbt.eduvision.eduvision360.ai.controller.AiTutorController;
import lk.icbt.eduvision.eduvision360.ai.service.AiTutorService;
import lk.icbt.eduvision.eduvision360.quiz.controller.TeacherQuizController;
import lk.icbt.eduvision.eduvision360.quiz.dto.TeacherQuizResponse;
import lk.icbt.eduvision.eduvision360.quiz.service.TeacherQuizService;
import lk.icbt.eduvision.eduvision360.security.config.SecurityConfig;
import lk.icbt.eduvision.eduvision360.security.jwt.JwtAuthFilter;
import lk.icbt.eduvision.eduvision360.security.jwt.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        AdminController.class,
        TeacherQuizController.class,
        AiTutorController.class
})
@AutoConfigureMockMvc
@Import({
        SecurityConfig.class,
        SecurityAccessTest.TestSecurityBeans.class
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Batch 4 - Security and RBAC Automated Tests")
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepo;

    @MockitoBean
    private EmailSender emailSender;

    @MockitoBean
    private TeacherQuizService teacherQuizService;

    @MockitoBean
    private AiTutorService aiTutorService;

    @TestConfiguration
    static class TestSecurityBeans {
        @Bean
        JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }

        @Bean
        JwtAuthFilter jwtAuthFilter(JwtService jwtService) {
            return new JwtAuthFilter(jwtService);
        }
    }

    @Test
    @Order(25)
    @DisplayName("ATC-25: Anonymous user should be blocked from an admin-only endpoint")
    void anonymousUser_shouldBeBlocked_fromAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(26)
    @WithMockUser(authorities = "STUDENT")
    @DisplayName("ATC-26: Student should be blocked from an admin-only endpoint")
    void student_shouldBeBlocked_fromAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(27)
    @WithMockUser(authorities = "TEACHER")
    @DisplayName("ATC-27: Teacher should be blocked from an admin-only endpoint")
    void teacher_shouldBeBlocked_fromAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(28)
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("ATC-28: Admin should be allowed to access an admin-only endpoint")
    void admin_shouldBeAllowed_toAccessAdminEndpoint() throws Exception {
        when(userRepo.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(29)
    @WithMockUser(authorities = "STUDENT")
    @DisplayName("ATC-29: Student should be blocked from a teacher-only endpoint")
    void student_shouldBeBlocked_fromTeacherEndpoint() throws Exception {
        mockMvc.perform(get("/api/teacher/quizzes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(30)
    @WithMockUser(authorities = "TEACHER")
    @DisplayName("ATC-30: Teacher should be allowed to access a teacher-only endpoint")
    void teacher_shouldBeAllowed_toAccessTeacherEndpoint() throws Exception {
        when(teacherQuizService.getMyQuizzes(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/teacher/quizzes"))
                .andExpect(status().isOk());
    }
}