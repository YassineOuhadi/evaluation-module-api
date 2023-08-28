package ma.caftech.sensipro.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.caftech.sensipro.domain.FillBlanksQuestion;
import ma.caftech.sensipro.domain.Language;
import ma.caftech.sensipro.domain.TrueFalseQuestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@JsonSerialize(using = QuestionDTOSerializer.class)
public class FillBlanksQuestionDTO extends QuestionDTO {

    private boolean isDragWords;

    public static FillBlanksQuestionDTO fromFillBlanksQuestion(FillBlanksQuestion fillBlanksQuestion) {
        FillBlanksQuestionDTO dto = new FillBlanksQuestionDTO();
        dto.setId(fillBlanksQuestion.getId());
        dto.setCode(fillBlanksQuestion.getCode());
        dto.setType(fillBlanksQuestion.getType());
        dto.setText(fillBlanksQuestion.getText());
        dto.setCorrectAnswerTip(fillBlanksQuestion.getCorrectAnswerTip());
        dto.setIncorrectAnswerTip(fillBlanksQuestion.getIncorrectAnswerTip());

        Set<CourseDTO> courseDTOs = fillBlanksQuestion.getCourses().stream()
                .map(course -> CourseDTO.fromCourse(course))
                .collect(Collectors.toSet());
        dto.setCourses(courseDTOs);

        Language language = fillBlanksQuestion.getLanguage();
        LanguageDTO languageDTO = new LanguageDTO();
        languageDTO.setId(language.getId());
        languageDTO.setName(language.getName());
        dto.setLanguage(languageDTO);

        dto.setDragWords(fillBlanksQuestion.isDragWords());

        return dto;
    }

    public String transformTextWithAsterisks() {
        String text = getText();
        String transformedText = text.replaceAll("\\*\\*([^*]+)\\*\\*", "****");
        return transformedText;
    }

    public List<String> extractBlocksFromText() {
        List<String> blocks = new ArrayList<>();
        String text = getText();
        int start = 0;
        int asterisksStart = text.indexOf("**", start);
        while (asterisksStart != -1) {
            int asterisksEnd = text.indexOf("**", asterisksStart + 2);
            if (asterisksEnd == -1) {
                break;
            }
            String block = text.substring(asterisksStart + 2, asterisksEnd);
            blocks.add(block);
            start = asterisksEnd + 2;
            asterisksStart = text.indexOf("**", start);
        }
        return blocks;
    }
}
