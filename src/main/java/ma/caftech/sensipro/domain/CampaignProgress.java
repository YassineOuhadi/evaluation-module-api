package ma.caftech.sensipro.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "campaign_progress")
@Data
public class CampaignProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Integer id;

    @Column(name = "value", columnDefinition = "integer default 0")
    private Integer value;

    @Column(name = "score", columnDefinition = "integer default 0")
    private Integer score;

    @Column(name = "start_date", columnDefinition = "timestamp default current_timestamp")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "correct_answers", columnDefinition = "integer default 0")
    private Integer correctAnswers;

    @Column(name = "nbre_attempts_exam", columnDefinition = "integer default 0")
    private Integer nbreAttemptsExam;

    @ManyToOne(optional = false)
    @JsonIgnore
    private AsUser asUser;

    @ManyToOne(optional = false)
    @JsonIgnore
    private LaunchCampaign launchCampaign;
}
