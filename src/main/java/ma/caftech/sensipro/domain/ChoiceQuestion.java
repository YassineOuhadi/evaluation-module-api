package ma.caftech.sensipro.domain;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@Entity
@Table(name = "choice_question")
public class ChoiceQuestion extends Question implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "is_multiple_choice", nullable = false, columnDefinition = "boolean default false")
    private boolean isMultipleChoice;

    @OneToMany(mappedBy = "choiceQuestion", cascade = CascadeType.ALL)
    private List<Option> options;

    public ChoiceQuestion() {
        setType(QuestionType.CHOICE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChoiceQuestion question = (ChoiceQuestion) o;
        return super.getId() != null ? super.getId().equals(question.getId()) : false;
    }

    @Override
    public int hashCode() {
        return super.getId() != null ? super.getId().hashCode() : 0;
    }
}
