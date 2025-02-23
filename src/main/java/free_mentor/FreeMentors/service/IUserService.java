/*
Group 18
 */


package free_mentor.FreeMentors.service;

import free_mentor.FreeMentors.dto.LoginRequest;
import free_mentor.FreeMentors.dto.SignupRequest;
import free_mentor.FreeMentors.entity.User;

public interface IUserService {
    User authenticateUser(LoginRequest request);
    User createUser(SignupRequest request);
    User findByEmail(String email);
}
