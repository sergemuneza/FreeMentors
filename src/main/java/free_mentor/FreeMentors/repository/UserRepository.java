package free_mentor.FreeMentors.repository;

import free_mentor.FreeMentors.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);



    @Query("SELECT u FROM User u WHERE u.role = free_mentor.FreeMentors.entity.Role.MENTOR")
    List<User> findAllMentors();

    // Fetch a single mentor by ID
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.role = free_mentor.FreeMentors.entity.Role.MENTOR")
    Optional<User> findMentorById(@Param("id") Long id);


}


