package lk.icbt.eduvision.eduvision360.classsession.scheduler;

import lk.icbt.eduvision.eduvision360.classsession.model.ClassSession;
import lk.icbt.eduvision.eduvision360.classsession.model.ClassSessionStatus;
import lk.icbt.eduvision.eduvision360.classsession.repository.ClassSessionRepository;
import lk.icbt.eduvision.eduvision360.classsession.service.ClassSessionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;

@Component
@RequiredArgsConstructor
public class ClassSessionAutomationScheduler {

    private final ClassSessionRepository repo;
    private final ClassSessionServiceImpl service;

    // Runs every minute
    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Colombo")
    public void runEveryMinute() {
        ZoneId zone = ZoneId.of("Asia/Colombo");
        ZonedDateTime now = ZonedDateTime.now(zone).withSecond(0).withNano(0);

        // 1) AUTO-OPEN
        for (ClassSession s : repo.findByStatusOrderBySessionDateDescStartTimeDesc(ClassSessionStatus.SCHEDULED)) {
            if (s.getSessionDate() == null || s.getStartTime() == null || s.getEndTime() == null) continue;

            ZonedDateTime start = ZonedDateTime.of(LocalDateTime.of(s.getSessionDate(), s.getStartTime()), zone);
            ZonedDateTime end   = ZonedDateTime.of(LocalDateTime.of(s.getSessionDate(), s.getEndTime()), zone);

            if (!now.isBefore(start) && now.isBefore(end)) {
                service.updateSessionStatusSystem(s.getId(), ClassSessionStatus.OPEN);
            }

            // If scheduler missed it and now is after end, complete it
            if (!now.isBefore(end)) {
                service.updateSessionStatusSystem(s.getId(), ClassSessionStatus.COMPLETED);
            }
        }

        // 2) AUTO-COMPLETE + 3) CLOSING SOON WARNINGS
        for (ClassSession s : repo.findByStatusOrderBySessionDateDescStartTimeDesc(ClassSessionStatus.OPEN)) {
            if (s.getSessionDate() == null || s.getEndTime() == null) continue;

            ZonedDateTime end = ZonedDateTime.of(LocalDateTime.of(s.getSessionDate(), s.getEndTime()), zone);

            // closing soon = last 10 minutes
            ZonedDateTime warnAt = end.minusMinutes(10);

            if (!now.isBefore(warnAt) && now.isBefore(end)) {
                long minutesLeft = Math.max(1, Duration.between(now, end).toMinutes());
                service.sendClosingSoonWarningsSystem(s.getId(), minutesLeft);
            }

            if (!now.isBefore(end)) {
                service.updateSessionStatusSystem(s.getId(), ClassSessionStatus.COMPLETED);
            }
        }
    }
}