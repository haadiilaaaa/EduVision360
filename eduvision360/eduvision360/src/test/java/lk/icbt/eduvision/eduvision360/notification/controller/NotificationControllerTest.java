package lk.icbt.eduvision.eduvision360.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.icbt.eduvision.eduvision360.notification.dto.NotificationResponse;
import lk.icbt.eduvision.eduvision360.notification.model.NotificationPriority;
import lk.icbt.eduvision.eduvision360.notification.model.NotificationType;
import lk.icbt.eduvision.eduvision360.notification.service.NotificationService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Batch 6A - Notification Controller Automated Tests")
class NotificationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private Authentication auth;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders
                .standaloneSetup(notificationController)
                .build();

        auth = new UsernamePasswordAuthenticationToken("student1", null);
    }

    @Test
    @Order(37)
    @DisplayName("ATC-37: Notification list endpoint should return current user notifications")
    void getMyNotifications_shouldReturnNotifications() throws Exception {
        List<NotificationResponse> notifications = List.of(
                new NotificationResponse(
                        "n1",
                        "New alert",
                        "You have a new alert",
                        NotificationType.SYSTEM_ALERT,
                        NotificationPriority.HIGH,
                        false,
                        null,
                        "e1",
                        "ENTITY",
                        "/student",
                        Instant.now()
                )
        );

        when(notificationService.getMyNotifications(any(Authentication.class), eq(false), eq(50)))
                .thenReturn(notifications);

        mockMvc.perform(get("/api/notifications/my").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("n1"))
                .andExpect(jsonPath("$[0].title").value("New alert"))
                .andExpect(jsonPath("$[0].message").value("You have a new alert"));
    }

    @Test
    @Order(38)
    @DisplayName("ATC-38: Unread notification count endpoint should return correct count")
    void getMyUnreadCount_shouldReturnCount() throws Exception {
        when(notificationService.getMyUnreadCount(any(Authentication.class))).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/my/unread-count").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(5));
    }

    @Test
    @Order(39)
    @DisplayName("ATC-39: Mark notification as read endpoint should update notification state")
    void markAsRead_shouldReturnUpdatedNotification() throws Exception {
        NotificationResponse response = new NotificationResponse(
                "n1",
                "New alert",
                "You have a new alert",
                NotificationType.SYSTEM_ALERT,
                NotificationPriority.HIGH,
                true,
                Instant.now(),
                "e1",
                "ENTITY",
                "/student",
                Instant.now()
        );

        when(notificationService.markAsRead(eq("n1"), any(Authentication.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/notifications/n1/read").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("n1"))
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    @Order(40)
    @DisplayName("ATC-40: Mark all notifications as read endpoint should update all unread notifications")
    void markAllAsRead_shouldReturnSuccessMessage() throws Exception {
        doNothing().when(notificationService).markAllAsRead(any(Authentication.class));

        mockMvc.perform(put("/api/notifications/my/read-all").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All notifications marked as read"));
    }
}