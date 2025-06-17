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

    @Column(nullable = false, length = 1000)
    private String text;

    private String hashtags;

    @Column(name = "image_url")
    private String imageUrl;

    private String platform;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    private boolean published;

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
