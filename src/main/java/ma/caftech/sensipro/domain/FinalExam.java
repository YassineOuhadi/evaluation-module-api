package ma.caftech.sensipro.domain;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;


@NamedQuery(name = "FinalExam.findSession", query = "SELECT q FROM FinalExam q WHERE q.user = :user and q.campaign = :campaign and q.isCompleted = :isCompleted")
@NamedQuery(name = "FinalExam.findLastExamSessionByUserAndCampaign",
            query = "SELECT q FROM FinalExam q WHERE q.user = :user AND q.campaign = :campaign AND q.startTime = (SELECT MAX(e.startTime) FROM FinalExam e WHERE e.user = :user AND e.campaign = :campaign)")
@Entity
@Data
public class FinalExam { // user exam session, possible redo exam

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false) // This enforces the constraint
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false) // This enforces the constraint
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(name = "is_completed", columnDefinition = "boolean default false")
    private boolean isCompleted;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(name = "exam_score", columnDefinition = "double default 0")
    private double examScore; // Exam score (in %)

    /*
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "used_question",
            joinColumns = @JoinColumn(name = "final_exam_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<Question> usedQuestions = new HashSet<>();
    */

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FinalExam finalExam = (FinalExam) o;
        return Objects.equals(id, finalExam.id);
    }


    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL)
    private Set<ExamQuestion> examQuestions = new HashSet<>();
}
