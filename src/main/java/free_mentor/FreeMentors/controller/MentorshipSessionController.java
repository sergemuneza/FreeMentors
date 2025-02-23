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
            validateToken(authHeader);
            String token = authHeader.substring(7);
            Long menteeId = jwtUtil.extractUserId(token);

            Long mentorId = ((Number) request.get("mentorId")).longValue();
            String questions = (String) request.get("questions");

            MentorshipSession session = sessionService.createSession(menteeId, mentorId, questions);

            response.put("status", HttpStatus.CREATED.value());
            Map<String, Object> data = Map.of(
                    "sessionId", session.getId(),
                    "mentorId", session.getMentor().getId(),
                    "menteeId", session.getMentee().getId(),
                    "questions", session.getQuestions(),
                    "menteeEmail", session.getMenteeEmail(),
                    "status", session.getStatus().toString()
            );
            response.put("data", data);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return handleException(response, e, HttpStatus.BAD_REQUEST);
        }
    }

    /*
     Accept a mentorship session (mentors only).
     */
    @PatchMapping("/{sessionId}/accept")
    public ResponseEntity<Map<String, Object>> acceptSession(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long sessionId) {
        return handleSessionUpdate(authHeader, sessionId, "APPROVED");
    }

    /*
     Reject a mentorship session (mentors only).
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
            validateToken(authHeader);
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String userRole = jwtUtil.extractUserRole(token);

            List<MentorshipSession> sessions = switch (userRole.toUpperCase()) {
                case "MENTOR" -> sessionService.getSessionsByMentorId(userId);
                case "MENTEE" -> sessionService.getSessionsByMenteeId(userId);
                default -> throw new IllegalArgumentException("Invalid user role");
            };

            if (sessions.isEmpty()) {
                response.put("status", HttpStatus.OK.value());
                response.put("data", "No sessions found for the user.");
                return ResponseEntity.ok(response);
            }

            List<Map<String, Object>> sessionData = sessions.stream()
                    .map(session -> {
                        Map<String, Object> sessionMap = new HashMap<>();
                        sessionMap.put("sessionId", session.getId());
                        sessionMap.put("mentorId", session.getMentor().getId());
                        sessionMap.put("menteeId", session.getMentee().getId());
                        sessionMap.put("questions", session.getQuestions());
                        sessionMap.put("menteeEmail", session.getMenteeEmail());
                        sessionMap.put("status", session.getStatus().toString());
                        return sessionMap;
                    })
                    .collect(Collectors.toList());

            response.put("status", HttpStatus.OK.value());
            response.put("data", sessionData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(response, e, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Review a mentorship session.
     */
    @PostMapping("/{sessionId}/review")
    public ResponseEntity<Map<String, Object>> reviewMentor(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long sessionId,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = new HashMap<>();
        try {
            validateToken(authHeader);
            String token = authHeader.substring(7);
            Long menteeId = jwtUtil.extractUserId(token);

            Integer score = (Integer) request.get("score");
            String remark = (String) request.get("remark");

            if (score == null || score < 1 || score > 5) {
                throw new IllegalArgumentException("Invalid score. It must be between 1 and 5.");
            }

            MentorshipSession session = sessionService.reviewMentor(sessionId, menteeId, score, remark);

            ReviewResponseDTO reviewResponse = new ReviewResponseDTO(
                    session.getId(),
                    session.getMentor().getId(),
                    session.getMentee().getId(),
                    session.getMentee().getFirstName() + " " + session.getMentee().getLastName(),
                    score,
                    remark
            );

            response.put("status", HttpStatus.CREATED.value());
            response.put("data", reviewResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return handleException(response, e, HttpStatus.BAD_REQUEST);
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
            validateToken(authHeader);
            String token = authHeader.substring(7);
            Long menteeId = jwtUtil.extractUserId(token);

            sessionService.deleteReview(sessionId, menteeId);

            response.put("status", HttpStatus.OK.value());
            response.put("message", "Review successfully deleted");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(response, e, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Handle session updates (accept/reject) for mentors only.
     */
    private ResponseEntity<Map<String, Object>> handleSessionUpdate(
            String authHeader, Long sessionId, String status) {

        Map<String, Object> response = new HashMap<>();
        try {
            validateToken(authHeader);
            String token = authHeader.substring(7);
            Long mentorId = jwtUtil.extractUserId(token);
            String userRole = jwtUtil.extractUserRole(token);

            if (!"MENTOR".equalsIgnoreCase(userRole)) {
                throw new IllegalArgumentException("Invalid user role. Only mentors can perform this action.");
            }

            MentorshipSession session = sessionService.updateSessionStatus(sessionId, mentorId, status);

            Map<String, Object> data = Map.of(
                    "sessionId", session.getId(),
                    "mentorId", session.getMentor().getId(),
                    "menteeId", session.getMentee().getId(),
                    "questions", session.getQuestions(),
                    "menteeEmail", session.getMenteeEmail(),
                    "status", session.getStatus()
            );

            response.put("status", HttpStatus.OK.value());
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(response, e, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Validate the Authorization header.
     */
    private void validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid token");
        }
    }

    /**
     * Handle exceptions uniformly.
     */
    private ResponseEntity<Map<String, Object>> handleException(
            Map<String, Object> response, Exception e, HttpStatus status) {
        response.put("error", e.getMessage());
        return ResponseEntity.status(status).body(response);
    }
}




