/*
Group 18
 */


package free_mentor.FreeMentors.controller;

import free_mentor.FreeMentors.dto.LoginRequest;
import free_mentor.FreeMentors.dto.SignupRequest;
import free_mentor.FreeMentors.entity.User;
import free_mentor.FreeMentors.exception.AuthenticationException;
import free_mentor.FreeMentors.service.UserServiceImpl;
import free_mentor.FreeMentors.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody SignupRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Check if the email already exists
            User user = userService.createUser(request);
            response.put("message", "User created successfully");
            response.put("user", user);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            // Handle duplicate email error
            response.put("status", HttpStatus.CONFLICT.value()); // 409 Conflict
            response.put("error", "Email already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> signin(@Valid @RequestBody LoginRequest loginRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.authenticateUser(loginRequest);

            // Generate token including userId
            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

            response.put("message", "User successfully logged in");
            response.put("token", token);
            response.put("userId", user.getId());
            //response.put("role", user.getRole().name());
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            response.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("error", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
