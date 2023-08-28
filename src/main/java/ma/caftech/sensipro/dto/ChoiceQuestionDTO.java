package ma.caftech.sensipro.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.caftech.sensipro.domain.ChoiceQuestion;
import ma.caftech.sensipro.domain.Language;
import ma.caftech.sensipro.domain.Option;
import ma.caftech.sensipro.domain.TrueFalseQuestion;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@JsonSerialize(using = QuestionDTOSerializer.class)
public class ChoiceQuestionDTO extends QuestionDTO {

    private boolean isMultipleChoice;
    private List<Option> options;

    public static ChoiceQuestionDTO fromChoiceQuestionDTO(ChoiceQuestion choiceQuestion) {
        ChoiceQuestionDTO dto = new ChoiceQuestionDTO();
        dto.setId(choiceQuestion.getId());
        dto.setCode(choiceQuestion.getCode());
        dto.setType(choiceQuestion.getType());
        dto.setText(choiceQuestion.getText());
        dto.setCorrectAnswerTip(choiceQuestion.getCorrectAnswerTip());

        dto.setIncorrectAnswerTip(choiceQuestion.getIncorrectAnswerTip());

        Set<CourseDTO> courseDTOs = choiceQuestion.getCourses().stream()
                .map(course -> CourseDTO.fromCourse(course))
                .collect(Collectors.toSet());
        dto.setCourses(courseDTOs);

        Language language = choiceQuestion.getLanguage();
        LanguageDTO languageDTO = new LanguageDTO();
        languageDTO.setId(language.getId());
        languageDTO.setName(language.getName());
        dto.setLanguage(languageDTO);

        dto.setMultipleChoice(choiceQuestion.isMultipleChoice());
        dto.setOptions(choiceQuestion.getOptions());

        return dto;
    }
}
