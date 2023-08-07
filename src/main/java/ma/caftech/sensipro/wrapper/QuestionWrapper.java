package ma.caftech.sensipro.wrapper;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ma.caftech.sensipro.FactoryPattern.QuestionFactory;
import ma.caftech.sensipro.domain.Question;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionWrapper {

    @JsonSerialize(using = QuestionSerializer.class)
    private Question question;
    private QuestionFactory.QuestionType type;

    public QuestionWrapper(Question question, QuestionFactory.QuestionType type) {
        this.question = question;
        this.type = type;
    }
}