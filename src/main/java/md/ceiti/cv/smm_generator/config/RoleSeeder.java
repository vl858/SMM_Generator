package md.ceiti.cv.smm_generator.config;

import jakarta.annotation.PostConstruct;
import md.ceiti.cv.smm_generator.entity.Role;
import md.ceiti.cv.smm_generator.repository.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleSeeder {

    private final RoleRepository roleRepository;

    public RoleSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void initRoles() {
        List<String> rolesNames = List.of("ROLE_ADMIN", "ROLE_USER");

        for (String roleName : rolesNames) {
            if(roleRepository.findByName(roleName) == null) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }
    }
}
