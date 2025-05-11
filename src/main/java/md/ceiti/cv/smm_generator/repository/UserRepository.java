package md.ceiti.cv.smm_generator.repository;

import md.ceiti.cv.smm_generator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
