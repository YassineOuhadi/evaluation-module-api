package ma.caftech.sensipro.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "course_progress")
public class CourseProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Long id;

    @Column(name = "value", columnDefinition = "integer default 0")
    private Integer value;

    @Column(name = "start_date", columnDefinition = "timestamp default current_timestamp")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToOne(optional = false)
    @JsonIgnore
    private Course course;

    @ManyToOne(optional = false)
    @JsonIgnore
    private CampaignProgress campaignProgress;
}
