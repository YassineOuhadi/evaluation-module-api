package ma.caftech.sensipro.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "integer default 11")
    private Integer examNumberQuestions;

    @Column(columnDefinition = "integer default 3")
    private Integer examNumberTentatives;

    @Column(name = "required_exam_score", columnDefinition = "double default 0")
    private double requiredExamScore; // Required exam score to achieve for archiving (in %)

    @Column(name = "test_open_duration")
    private Integer testOpenDuration; // Duration (in minutes) for which the test is open after completion

    @Column(name = "retry_test_duration")
    private Integer retryTestDuration; // Duration (in hours) to wait before retrying the test

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Course> courses = new ArrayList<>();
}