/*
Group 18
 */


package free_mentor.FreeMentors.entity;

import jakarta.persistence.*;
import lombok.*;
import free_mentor.FreeMentors.entity.SessionStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorshipSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @ManyToOne
    @JoinColumn(name = "mentee_id", nullable = false)
    private User mentee;

    @Column(nullable = false)
    private String questions;

    @Column(nullable = false)
    private String menteeEmail;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Column(nullable = true)
    private Integer score;

    @Column(length = 500, nullable = true)
    private String remark;

}
