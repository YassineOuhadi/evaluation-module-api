package ma.caftech.sensipro.domain;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "true_false_question")
public class TrueFalseQuestion extends Question implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "is_correct", nullable = false, columnDefinition = "boolean default false")
    private boolean isCorrect;

    public TrueFalseQuestion() {
        setType(QuestionType.TRUE_FALSE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrueFalseQuestion question = (TrueFalseQuestion) o;
        return super.getId() != null ? super.getId().equals(question.getId()) : false;
    }

    @Override
    public int hashCode() {
        return super.getId() != null ? super.getId().hashCode() : 0;
    }
}
