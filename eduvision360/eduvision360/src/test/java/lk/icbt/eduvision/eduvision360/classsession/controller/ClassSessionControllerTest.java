package lk.icbt.eduvision.eduvision360.classsession.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.icbt.eduvision.eduvision360.classsession.service.ClassSessionService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Batch 5A - Class Session Controller Automated Tests")
class ClassSessionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ClassSessionService classSessionService;

    @InjectMocks
    private ClassSessionController classSessionController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(classSessionController)
                .setValidator(validator)
                .build();
    }

    @Test
    @Order(31)
    @DisplayName("ATC-31: Teacher should be able to create a class session with valid input")
    void createSession_shouldSucceed_withValidPayload() throws Exception {
        String validJson = """
                {
                  "courseId": "COURSE001",
                  "sessionDate": "2026-03-10",
                  "startTime": "10:00:00",
                  "endTime": "12:00:00"
                }
                """;

        when(classSessionService.createSession(any(), any()))
                .thenReturn(null);

        mockMvc.perform(post("/api/class-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(32)
    @DisplayName("ATC-32: Class session creation should fail when the payload is invalid")
    void createSession_shouldFail_withInvalidPayload() throws Exception {
        String invalidJson = """
                {
                  "courseId": "",
                  "sessionDate": null,
                  "startTime": "10:00:00",
                  "endTime": "12:00:00"
                }
                """;

        mockMvc.perform(post("/api/class-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(33)
    @DisplayName("ATC-33: Student should be able to retrieve their own session list")
    void getStudentSessions_shouldReturnSessionList() throws Exception {
        when(classSessionService.getStudentSessions(any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/class-sessions/student/my"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}