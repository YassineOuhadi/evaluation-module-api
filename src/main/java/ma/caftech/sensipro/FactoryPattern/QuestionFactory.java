package ma.caftech.sensipro.FactoryPattern;

import ma.caftech.sensipro.domain.ChoiceQuestion;
import ma.caftech.sensipro.domain.FillBlanksQuestion;
import ma.caftech.sensipro.domain.Question;
import ma.caftech.sensipro.domain.TrueFalseQuestion;

public class QuestionFactory {
    public enum QuestionType {
        TRUE_FALSE,
        CHOICE,
        FILL_BLANKS
    }

    public static Question createQuestion(QuestionType type) {
        switch (type) {
            case TRUE_FALSE:
                return new TrueFalseQuestion();
            case CHOICE:
                return new ChoiceQuestion();
            case FILL_BLANKS:
                return new FillBlanksQuestion();
            default:
                throw new IllegalArgumentException("Invalid question type");
        }
    }
}