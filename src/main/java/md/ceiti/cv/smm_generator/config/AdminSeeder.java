package md.ceiti.cv.smm_generator.config;

import jakarta.annotation.PostConstruct;
import md.ceiti.cv.smm_generator.entity.Role;
import md.ceiti.cv.smm_generator.entity.User;
import md.ceiti.cv.smm_generator.repository.RoleRepository;
import md.ceiti.cv.smm_generator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AdminSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final RoleRepository roleRepository;

    public AdminSeeder(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Value("${app.default-admin.email}")
    private String adminEmail;

    @Value("${app.default-admin.password}")
    private String adminPassword;

    @Value("${app.default-admin.firstname}")
    private String adminFirstname;

    @Value("${app.default-admin.lastname}")
    private String adminLastname;

    @PostConstruct
    public void initAdmin() {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {

            Role adminRole = roleRepository.findByName("ROLE_ADMIN");
            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setName("ROLE_ADMIN");
                adminRole.setUsers(new ArrayList<>());
                roleRepository.save(adminRole);
            }

            User admin = new User();
            admin.setName(adminFirstname + " " + adminLastname);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRoles(List.of(adminRole));

            userRepository.save(admin);

            System.out.println("Admin created automatically: " + adminEmail);
        } else {
            System.out.println("Admin already exists: " + adminEmail);
        }
    }
}
