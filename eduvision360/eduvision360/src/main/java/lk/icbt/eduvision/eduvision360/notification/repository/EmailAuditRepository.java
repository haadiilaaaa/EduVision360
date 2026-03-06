package lk.icbt.eduvision.eduvision360.notification.repository;

import lk.icbt.eduvision.eduvision360.notification.model.EmailAudit;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmailAuditRepository extends MongoRepository<EmailAudit, String> {
}