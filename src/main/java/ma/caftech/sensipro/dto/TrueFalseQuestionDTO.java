package ma.caftech.sensipro.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TrueFalseQuestionDTO extends QuestionDTO {

    private Boolean isCorrect;
}
