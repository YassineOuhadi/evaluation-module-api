package ma.caftech.sensipro.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.caftech.sensipro.domain.Language;
import ma.caftech.sensipro.domain.TrueFalseQuestion;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@JsonSerialize(using = QuestionDTOSerializer.class)
public class TrueFalseQuestionDTO extends QuestionDTO {

    private boolean isCorrect;

    public static TrueFalseQuestionDTO fromTrueFalseQuestion(TrueFalseQuestion trueFalseQuestion) {
        TrueFalseQuestionDTO dto = new TrueFalseQuestionDTO();
        dto.setId(trueFalseQuestion.getId());
        dto.setCode(trueFalseQuestion.getCode());
        dto.setType(trueFalseQuestion.getType());
        dto.setText(trueFalseQuestion.getText());
        dto.setCorrectAnswerTip(trueFalseQuestion.getCorrectAnswerTip());

        dto.setIncorrectAnswerTip(trueFalseQuestion.getIncorrectAnswerTip());
        Set<CourseDTO> courseDTOs = trueFalseQuestion.getCourses().stream()
                .map(course -> CourseDTO.fromCourse(course))
                .collect(Collectors.toSet());
        dto.setCourses(courseDTOs);

        Language language = trueFalseQuestion.getLanguage();
        LanguageDTO languageDTO = new LanguageDTO();
        languageDTO.setId(language.getId());
        languageDTO.setName(language.getName());
        dto.setLanguage(languageDTO);

        dto.setCorrect(trueFalseQuestion.isCorrect());

        return dto;
    }
}
