package free_mentor.FreeMentors.controller;

import free_mentor.FreeMentors.entity.User;
import free_mentor.FreeMentors.entity.Role;
import free_mentor.FreeMentors.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/mentors")
public class MentorController {

    @Autowired
    private UserRepository userRepository;

    // Promote a user to a mentor (Admin only)
    @PatchMapping("/{userId}/promote")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, String>> promoteToMentor(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.MENTOR) {
            throw new RuntimeException("User is already a mentor");
        }

        user.setRole(Role.MENTOR);
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "User successfully promoted to mentor");
        return ResponseEntity.ok(response);


    }

    @GetMapping
    public ResponseEntity<List<User>> getAllMentors() {
        List<User> mentors = userRepository.findAllMentors();
        if (mentors.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(mentors);
    }

    // Get a specific mentor by ID
    @GetMapping("/{mentorId}")
    public ResponseEntity<User> getMentorById(@PathVariable Long mentorId) {
        Optional<User> mentor = userRepository.findMentorById(mentorId);
        return mentor.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}