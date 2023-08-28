package ma.caftech.sensipro.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.caftech.sensipro.domain.Course;
import ma.caftech.sensipro.domain.Language;
import ma.caftech.sensipro.domain.Question;

import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class QuestionDTO {

    private Integer id;
    private String code;
    private Question.QuestionType type;
    private String text;
    private String correctAnswerTip;
    private String incorrectAnswerTip;
    private Set<CourseDTO> courses;
    private LanguageDTO language;

    public Map<String, Object> getLanguageInfo() {
        Map<String, Object> languageInfo = new HashMap<>();
        languageInfo.put("id", language.getId());
        languageInfo.put("name", language.getName());
        return languageInfo;
    }
}