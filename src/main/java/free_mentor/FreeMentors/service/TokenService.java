package free_mentor.FreeMentors.service;

import free_mentor.FreeMentors.entity.User;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    public User decodeToken(String token) {
        // Mock decoding logic. Replace this with actual implementation.
        User user = new User();
        user.setId(3L); // Assume token corresponds to user ID 3.
        return user;
    }
}
