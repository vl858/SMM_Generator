package md.ceiti.cv.smm_generator.repository;

import md.ceiti.cv.smm_generator.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByName(String name);
}
