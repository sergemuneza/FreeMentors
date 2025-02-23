/*
Group 18
 */


package free_mentor.FreeMentors.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseDTO {
    private Long sessionId;
    private Long mentorId;
    private Long menteeId;
    private String menteeFullName; // Combined full name
    private Integer score;
    private String remark;
}
