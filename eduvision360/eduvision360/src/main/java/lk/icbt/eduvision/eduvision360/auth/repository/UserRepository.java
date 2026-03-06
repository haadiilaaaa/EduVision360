package lk.icbt.eduvision.eduvision360.auth.repository;

import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.model.UserRole;
import lk.icbt.eduvision.eduvision360.auth.model.UserStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // ✅ needed for safe student code generation
    boolean existsByStudentCode(String studentCode);

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    List<User> findByRoleAndStatus(UserRole role, UserStatus status);
    long countByRole(UserRole role);
    Optional<User> findByEmailIgnoreCase(String email);
}