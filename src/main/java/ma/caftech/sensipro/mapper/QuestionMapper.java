package ma.caftech.sensipro.mapper;

import ma.caftech.sensipro.domain.*;
import ma.caftech.sensipro.dto.ChoiceQuestionDTO;
import ma.caftech.sensipro.dto.FillBlanksQuestionDTO;
import ma.caftech.sensipro.dto.QuestionDTO;
import ma.caftech.sensipro.dto.TrueFalseQuestionDTO;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for the entity {@link Question} and its DTO {@link QuestionDTO}.
 */
@Mapper(componentModel = "spring")
public interface QuestionMapper {

    ChoiceQuestion toChoiceQuestion(ChoiceQuestionDTO choiceQuestionDTO);

    FillBlanksQuestion toFillBlanksQuestion(FillBlanksQuestionDTO fillBlanksQuestionDTO);

    TrueFalseQuestion toTrueFalseQuestion(TrueFalseQuestionDTO trueFalseQuestionDTO);

    ChoiceQuestionDTO toChoiceQuestionDTO(ChoiceQuestion choiceQuestion);

    FillBlanksQuestionDTO toFillBlanksQuestionDTO(FillBlanksQuestion fillBlanksQuestion);

    TrueFalseQuestionDTO toTrueFalseQuestionDTO(TrueFalseQuestion trueFalseQuestion);

    default Question toQuestion(QuestionDTO questionDTO) {
        if (questionDTO instanceof ChoiceQuestionDTO) {
            ChoiceQuestion choiceQuestion = toChoiceQuestion((ChoiceQuestionDTO) questionDTO);
            List<Option> options = choiceQuestion.getOptions();
            for (Option option : options) option.setChoiceQuestion(choiceQuestion);
            choiceQuestion.setOptions(options);
            return choiceQuestion;
        } else if (questionDTO instanceof FillBlanksQuestionDTO) {
            return toFillBlanksQuestion((FillBlanksQuestionDTO) questionDTO);
        } else if (questionDTO instanceof TrueFalseQuestionDTO) {
            return toTrueFalseQuestion((TrueFalseQuestionDTO) questionDTO);
        } else {
            throw new IllegalArgumentException("Unknown question type");
        }
    }

    default QuestionDTO toQuestionDTO(Question question) {
        if (question instanceof ChoiceQuestion) {
            ChoiceQuestionDTO choiceQuestionDTO = toChoiceQuestionDTO((ChoiceQuestion) question);
            for (Option option : choiceQuestionDTO.getOptions())
                option.setIsCorrect(null);
            return choiceQuestionDTO;
        } else if (question instanceof FillBlanksQuestion) {
            FillBlanksQuestionDTO fillBlanksQuestionDTO = toFillBlanksQuestionDTO((FillBlanksQuestion) question);
            fillBlanksQuestionDTO.setText(((FillBlanksQuestion) question).transformTextWithAsterisks());
            if (fillBlanksQuestionDTO.getIsDragWords()) {
                fillBlanksQuestionDTO.setHiddenWords(((FillBlanksQuestion) question).extractBlocksFromText(false));
            }
            return fillBlanksQuestionDTO;
        } else if (question instanceof TrueFalseQuestion) {
            TrueFalseQuestionDTO trueFalseQuestionDTO = toTrueFalseQuestionDTO((TrueFalseQuestion) question);
            trueFalseQuestionDTO.setIsCorrect(null);
            return trueFalseQuestionDTO;
        } else {
            throw new IllegalArgumentException("Unknown question type");
        }
    }
}