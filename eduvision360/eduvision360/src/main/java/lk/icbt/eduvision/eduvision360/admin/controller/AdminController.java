package lk.icbt.eduvision.eduvision360.admin.controller;

import lk.icbt.eduvision.eduvision360.admin.dto.AdminUserResponse;
import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.model.UserRole;
import lk.icbt.eduvision.eduvision360.auth.model.UserStatus;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.auth.service.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepo;
    private final EmailSender emailSender;

    // ------------------ PENDING TEACHERS ------------------

    @GetMapping("/users/pending-teachers")
    public List<AdminUserResponse> getPendingTeachers() {
        return userRepo.findByRoleAndStatus(
                        UserRole.TEACHER,
                        UserStatus.PENDING_APPROVAL
                ).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @PostMapping("/users/approve/{id}")
    public void approveTeacher(@PathVariable String id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(UserStatus.ACTIVE);
        if (user.getVerifiedAt() == null) {
            user.setVerifiedAt(Instant.now());
        }

        userRepo.save(user);
        emailSender.sendTeacherApprovedEmail(user.getEmail());
    }

    @PostMapping("/users/reject/{id}")
    public void rejectTeacher(@PathVariable String id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(UserStatus.DISABLED);
        userRepo.save(user);

        emailSender.sendTeacherRejectedEmail(user.getEmail());
    }

    // ------------------ MANAGE USERS ------------------

    @GetMapping("/users")
    public List<AdminUserResponse> getAllUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String search
    ) {
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase();

        return userRepo.findAll().stream()
                .filter(user -> role == null || user.getRole() == role)
                .filter(user -> status == null || user.getStatus() == status)
                .filter(user -> normalizedSearch.isBlank() || matchesSearch(user, normalizedSearch))
                .sorted(Comparator.comparing(
                        User::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .map(this::mapToResponse)
                .toList();
    }

    @PatchMapping("/users/{id}/status")
    public AdminUserResponse updateUserStatus(
            @PathVariable String id,
            @RequestParam UserStatus status
    ) {
        if (status != UserStatus.ACTIVE && status != UserStatus.DISABLED) {
            throw new IllegalArgumentException("Only ACTIVE or DISABLED status updates are allowed here");
        }

        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(status);

        if (status == UserStatus.ACTIVE && user.getVerifiedAt() == null) {
            user.setVerifiedAt(Instant.now());
        }

        userRepo.save(user);
        return mapToResponse(user);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable String id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalArgumentException("Admin accounts cannot be deleted");
        }

        userRepo.delete(user);
    }

    // ------------------ HELPERS ------------------

    private AdminUserResponse mapToResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .contactNumber(user.getContactNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .studentCode(user.getStudentCode())
                .createdAt(user.getCreatedAt())
                .verifiedAt(user.getVerifiedAt())
                .build();
    }

    private boolean matchesSearch(User user, String search) {
        return contains(user.getFullName(), search)
                || contains(user.getEmail(), search)
                || contains(user.getUsername(), search)
                || contains(user.getStudentCode(), search);
    }

    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase().contains(search);
    }
}