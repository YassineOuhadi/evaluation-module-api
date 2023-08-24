package ma.caftech.sensipro.domain.id;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class ExamQuestionId implements Serializable {

    private Integer exam;
    private Integer question;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExamQuestionId)) return false;
        ExamQuestionId that = (ExamQuestionId) o;
        return Objects.equals(exam, that.exam) &&
                Objects.equals(question, that.question);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exam,question);
    }
}
