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

    private Long id;
    private String code;
    private Question.QuestionType type;
    private String text;
    private String correctAnswerTip;
    private String incorrectAnswerTip;
    private Set<CourseDTO> courses;
    private LanguageDTO language;
}