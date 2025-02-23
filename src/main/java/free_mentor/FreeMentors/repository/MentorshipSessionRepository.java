/*
Group 18
 */


package free_mentor.FreeMentors.repository;


import free_mentor.FreeMentors.entity.MentorshipSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MentorshipSessionRepository extends JpaRepository<MentorshipSession, Long> {

    /*
     * Finds all mentorship sessions assigned to a specific mentor.
     *
     * @param mentorId The ID of the mentor
     * @return A list of mentorship sessions for the mentor
     */
    List<MentorshipSession> findByMentorId(Long mentorId);

    /*
     * Finds all mentorship sessions created by a specific mentee.
     *
     * @param menteeId The ID of the mentee
     * @return A list of mentorship sessions created by the mentee
     */
    List<MentorshipSession> findByMenteeId(Long menteeId);
}

