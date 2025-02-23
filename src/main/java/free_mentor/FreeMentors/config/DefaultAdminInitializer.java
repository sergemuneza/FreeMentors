/* package free_mentor.FreeMentors.config;

import free_mentor.FreeMentors.entity.User;
import free_mentor.FreeMentors.entity.Role;
import free_mentor.FreeMentors.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

@Component
public class DefaultAdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) {
        // Check if the admin user already exists
        if (!userRepository.existsByEmail("sergeadmin@freementor.com")) {
            // Create a new admin user
            User admin = new User();
            admin.setFirstName("Default");
            admin.setLastName("Admin");
            admin.setEmail("sergeadmin@freementor.com");
            admin.setPassword(new BCryptPasswordEncoder().encode("admin123")); // Encrypt password
            admin.setRole(Role.ADMIN); // Set role to ADMIN

            userRepository.save(admin);
            System.out.println("Default admin user created successfully!");
        } else {
            System.out.println("Admin user already exists.");
        }
    }
}

 */