package md.ceiti.cv.smm_generator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(columnDefinition = "TEXT")
    private String hashtags;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(length = 50)
    private String platform;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "published")
    private LocalDateTime published;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}