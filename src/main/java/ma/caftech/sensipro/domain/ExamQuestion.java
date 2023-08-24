package ma.caftech.sensipro.domain;

import lombok.Data;
import ma.caftech.sensipro.domain.id.CampaignUserId;
import ma.caftech.sensipro.domain.id.ExamQuestionId;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.json.JSONObject;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Data
@DynamicUpdate
@DynamicInsert
@IdClass(ExamQuestionId.class)
public class ExamQuestion {

    @Id
    @ManyToOne
    @JoinColumn(name = "exam_id")
    private FinalExam exam;

    @Id
    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @Lob
    @Column(name = "response", columnDefinition = "json")
    private JSONObject response;

    @Column(name = "is_correct", columnDefinition = "boolean default false")
    private boolean isCorrect;

    @Override
    public int hashCode() {
        return Objects.hash(exam, question); // Hash based on both exam and question
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExamQuestion that = (ExamQuestion) o;
        return Objects.equals(exam, that.exam) && Objects.equals(question, that.question);
    }
}
