package ma.caftech.sensipro.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class FillBlanksQuestionDTO extends QuestionDTO {

    private Boolean isDragWords;

    List<String> hiddenWords;
}
