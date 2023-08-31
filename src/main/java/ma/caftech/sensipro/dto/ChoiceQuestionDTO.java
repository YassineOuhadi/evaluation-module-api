package ma.caftech.sensipro.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ma.caftech.sensipro.domain.Option;

import java.util.List;

@Data
@NoArgsConstructor
public class ChoiceQuestionDTO extends QuestionDTO {

    private Boolean isMultipleChoice;
    private List<Option> options;
}
