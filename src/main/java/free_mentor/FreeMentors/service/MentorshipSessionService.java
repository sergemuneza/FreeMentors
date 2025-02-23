/*
Group 18
 */


package free_mentor.FreeMentors.service;

import free_mentor.FreeMentors.entity.MentorshipSession;
import free_mentor.FreeMentors.entity.Role;
import free_mentor.FreeMentors.entity.SessionStatus;
import free_mentor.FreeMentors.entity.User;
import free_mentor.FreeMentors.repository.MentorshipSessionRepository;
import free_mentor.FreeMentors.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MentorshipSessionService {

    private final MentorshipSessionRepository sessionRepository;
    private final UserRepository userRepository;

    /*
     * Creates a new mentorship session.
     *
     * @param menteeId  ID of the mentee requesting the session
     * @param mentorId  ID of the mentor for the session
     * @param questions Agenda or questions for the session
     * @return The created MentorshipSession object
     */
    public MentorshipSession createSession(Long menteeId, Long mentorId, String questions) {
        // Fetch mentee and mentor from the database
        User mentee = userRepository.findById(menteeId)
                .orElseThrow(() -> new RuntimeException("Mentee not found"));

        User mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));

        // Ensure the mentor has the correct role
        if (!mentor.getRole().equals(Role.MENTOR)) {
            throw new RuntimeException("User is not a mentor");
        }

        // Create and save the mentorship session
        MentorshipSession session = MentorshipSession.builder()
                .mentor(mentor)
                .mentee(mentee)
                .questions(questions)
                .menteeEmail(mentee.getEmail())
                .status(SessionStatus.PENDING)
                .build();

        return sessionRepository.save(session);
    }

    /*
     * Updates the status of an existing mentorship session.
     *
     * @param sessionId ID of the session to update
     * @param mentorId  ID of the mentor updating the session
     * @param status    New status of the session (e.g., "APPROVED", "REJECTED")
     * @return The updated MentorshipSession object
     * @throws RuntimeException if the session or mentor is invalid
     */
    public MentorshipSession updateSessionStatus(Long sessionId, Long mentorId, String status) {
        // Fetch the session from the database
        MentorshipSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Ensure the mentor updating the session is the session's mentor
        if (!session.getMentor().getId().equals(mentorId)) {
            throw new RuntimeException("Unauthorized to update this session");
        }

        // Validate the provided status
        SessionStatus sessionStatus;
        try {
            sessionStatus = SessionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid session status");
        }

        // Update the session's status
        session.setStatus(sessionStatus);
        return sessionRepository.save(session);
    }

    /*
     Fetches all mentorship sessions for a specific mentee.

     @param menteeId ID of the mentee
     @return List of MentorshipSession objects created by the mentee
     */
    public List<MentorshipSession> getSessionsByMenteeId(Long menteeId) {
        // Validate mentee existence
        userRepository.findById(menteeId)
                .orElseThrow(() -> new RuntimeException("Mentee not found"));

        // Fetch sessions
        return sessionRepository.findByMenteeId(menteeId);
    }

    /**
     * Fetches all mentorship sessions for a specific mentor.
     *
     * @param mentorId ID of the mentor
     * @return List of MentorshipSession objects requested against the mentor
     */
    public List<MentorshipSession> getSessionsByMentorId(Long mentorId) {
        // Validate mentor existence
        userRepository.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));

        // Fetch sessions
        return sessionRepository.findByMentorId(mentorId);
    }


    public MentorshipSession reviewMentor(Long sessionId, Long menteeId, Integer score, String remark) {
        // Fetch the session from the database
        MentorshipSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Check if the requester is the session's mentee
        if (!session.getMentee().getId().equals(menteeId)) {
            throw new RuntimeException("Unauthorized to review this session");
        }

        // Check if the session has been completed
        if (!(session.getStatus().equals(SessionStatus.REJECTED) || session.getStatus().equals(SessionStatus.APPROVED)))  {
            throw new RuntimeException("Cannot review a session that is not approved or completed");
        }

        // Add review details
        session.setScore(score);
        session.setRemark(remark);

        return sessionRepository.save(session);
    }


    public void deleteReview(Long sessionId, Long menteeId) {
        // Fetch the session
        MentorshipSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Validate that the request is made by the mentee
        if (!session.getMentee().getId().equals(menteeId)) {
            throw new RuntimeException("Unauthorized to delete this review");
        }

        // Ensure the session has a review to delete
        if (session.getScore() == null || session.getRemark() == null) {
            throw new RuntimeException("No review exists for this session");
        }

        // Remove the review fields
        session.setScore(null);
        session.setRemark(null);

        // Save the updated session
        sessionRepository.save(session);
    }

}
