package md.ceiti.cv.smm_generator.repository;

import md.ceiti.cv.smm_generator.entity.AiPost;
import md.ceiti.cv.smm_generator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiPostRepository extends JpaRepository<AiPost, Long> {
    List<AiPost> findAllByUser(User user);
}
