package lk.icbt.eduvision.eduvision360.institution.repository;

import lk.icbt.eduvision.eduvision360.institution.model.InstitutionSettings;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InstitutionSettingsRepository extends MongoRepository<InstitutionSettings, String> {
}