package lk.icbt.eduvision.eduvision360.communication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.icbt.eduvision.eduvision360.auth.model.UserRole;
import lk.icbt.eduvision.eduvision360.communication.dto.MessageResponse;
import lk.icbt.eduvision.eduvision360.communication.service.MessageService;
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
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Batch 6B - Teacher Message Controller Automated Tests")
class TeacherMessageControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private TeacherMessageController teacherMessageController;

    private Authentication auth;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(teacherMessageController)
                .setValidator(validator)
                .build();

        auth = new UsernamePasswordAuthenticationToken("teacher1", null);
    }

    @Test
    @Order(41)
    @DisplayName("ATC-41: Teacher should be able to send a message successfully")
    void sendMessage_shouldSucceed() throws Exception {
        String json = """
                {
                  "receiverId": "student1",
                  "subject": "Progress Update",
                  "body": "Please review this week's materials."
                }
                """;

        MessageResponse response = new MessageResponse(
                "m1",
                "teacher1",
                "Teacher One",
                UserRole.TEACHER,
                "student1",
                "Student One",
                UserRole.STUDENT,
                "Progress Update",
                "Please review this week's materials.",
                false,
                null,
                Instant.now()
        );

        when(messageService.sendTeacherMessage(any(), any(Authentication.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/teacher/messages")
                        .principal(auth)
                        .contentType(APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("m1"))
                .andExpect(jsonPath("$.subject").value("Progress Update"))
                .andExpect(jsonPath("$.receiverId").value("student1"));
    }
}