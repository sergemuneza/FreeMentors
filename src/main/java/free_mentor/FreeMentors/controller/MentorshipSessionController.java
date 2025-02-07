package free_mentor.FreeMentors.controller;

import free_mentor.FreeMentors.dto.ReviewResponseDTO;
import free_mentor.FreeMentors.entity.MentorshipSession;
import free_mentor.FreeMentors.service.MentorshipSessionService;
import free_mentor.FreeMentors.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class MentorshipSessionController {

    private final MentorshipSessionService sessionService;
    private final JwtUtil jwtUtil;

    /*
      Request a mentorship session.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> requestSession(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate & extract token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("error", "Missing or invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix
            Long menteeId = jwtUtil.extractUserId(token);

            // Extract request body data
            Long mentorId = ((Number) request.get("mentorId")).longValue();
            String questions = (String) request.get("questions");

            // Create mentorship session
            MentorshipSession session = sessionService.createSession(menteeId, mentorId, questions);

            // Build response
            response.put("status", 201);
            Map<String, Object> data = new HashMap<>();
            data.put("sessionId", session.getId());
            data.put("mentorId", session.getMentor().getId());
            data.put("menteeId", session.getMentee().getId());
            data.put("questions", session.getQuestions());
            data.put("menteeEmail", session.getMenteeEmail());
            data.put("status", session.getStatus().toString());

            response.put("data", data);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("error", "Invalid request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /*
      Accept a mentorship session.
     */
    @PatchMapping("/{sessionId}/accept")
    public ResponseEntity<Map<String, Object>> acceptSession(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long sessionId) {

        return handleSessionUpdate(authHeader, sessionId, "APPROVED");
    }

    /*
      Reject a mentorship session.
     */
    @PatchMapping("/{sessionId}/reject")
    public ResponseEntity<Map<String, Object>> rejectSession(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long sessionId) {


        return handleSessionUpdate(authHeader, sessionId, "REJECTED");
    }

    /*
      Get all sessions for the authenticated user (mentor or mentee).
     */
   @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getSessions(
            @RequestHeader("Authorization") String authHeader) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("error", "Missing or invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix
            Long userId = jwtUtil.extractUserId(token);
            String userRole = jwtUtil.extractUserRole(token);

            // Fetch sessions based on role
            List<MentorshipSession> sessions;
            if ("MENTOR".equalsIgnoreCase(userRole)) {
                sessions = sessionService.getSessionsByMentorId(userId);
            } else if ("MENTEE".equalsIgnoreCase(userRole)) {
                sessions = sessionService.getSessionsByMenteeId(userId);
            } else {
                response.put("error", "Invalid user role");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // If no sessions are found, return an appropriate response
            if (sessions.isEmpty()) {
                response.put("status", 200);
                response.put("data", "No sessions found for the user.");
                return ResponseEntity.ok(response);
            }

            // Build response with all sessions
            response.put("status", 200);
            List<Map<String, Object>> sessionData = sessions.stream().map(session -> {
                Map<String, Object> data = new HashMap<>();
                data.put("sessionId", session.getId());
                data.put("mentorId", session.getMentor().getId());
                data.put("menteeId", session.getMentee().getId());
                data.put("questions", session.getQuestions());
                data.put("menteeEmail", session.getMenteeEmail());
                data.put("status", session.getStatus().toString());
                return data;
            }).collect(Collectors.toList());

            response.put("data", sessionData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Failed to fetch sessions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    /*
     Review a mentorship session.
     */
    @PostMapping("/{sessionId}/review")
    public ResponseEntity<Map<String, Object>> reviewMentor(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long sessionId,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate & extract token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("error", "Missing or invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix
            Long menteeId = jwtUtil.extractUserId(token);

            // Extract request body data
            Integer score = (Integer) request.get("score");
            String remark = (String) request.get("remark");

            // Validate score
            if (score == null || score < 1 || score > 5) {
                response.put("error", "Invalid score. It must be between 1 and 5.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Submit review
            MentorshipSession session = sessionService.reviewMentor(sessionId, menteeId, score, remark);

            // Create ReviewResponseDTO
            ReviewResponseDTO reviewResponse = new ReviewResponseDTO(
                    session.getId(),
                    session.getMentor().getId(),
                    session.getMentee().getId(),
                    session.getMentee().getFirstName() + " " + session.getMentee().getLastName(),
                    score,
                    remark
            );

            // Build response
            response.put("status", 201);
            response.put("data", reviewResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("error", "Failed to submit review: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Delete a review for a mentorship session.
     */
    @DeleteMapping("/{sessionId}/review")
    public ResponseEntity<Map<String, Object>> deleteReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long sessionId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate & extract token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("error", "Missing or invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix
            Long menteeId = jwtUtil.extractUserId(token);

            // Call the service method to delete the review
            sessionService.deleteReview(sessionId, menteeId);

            // Prepare success response
            response.put("status", HttpStatus.OK.value());
            Map<String, String> data = new HashMap<>();
            data.put("message", "Review successfully deleted");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to delete review: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    private ResponseEntity<Map<String, Object>> handleSessionUpdate(
            String authHeader, Long sessionId, String status) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("error", "Missing or invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix
            Long mentorId = jwtUtil.extractUserId(token);

            // Update session status
            MentorshipSession session = sessionService.updateSessionStatus(sessionId, mentorId, status);

            // Build response
            Map<String, Object> data = new HashMap<>();
            data.put("sessionId", session.getId());
            data.put("mentorId", session.getMentor().getId());
            data.put("menteeId", session.getMentee().getId());
            data.put("questions", session.getQuestions());
            data.put("menteeEmail", session.getMenteeEmail());
            data.put("status", session.getStatus());

            response.put("status", 200);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Failed to update session: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}



