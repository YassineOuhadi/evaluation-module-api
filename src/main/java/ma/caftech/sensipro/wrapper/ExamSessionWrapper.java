package ma.caftech.sensipro.wrapper;

import lombok.Data;
import lombok.NoArgsConstructor;
import ma.caftech.sensipro.domain.FinalExam;

@Data
@NoArgsConstructor
public class ExamSessionWrapper {
    private FinalExam finalExam;
    private String message;

    public ExamSessionWrapper(FinalExam finalExam, String message) {
        this.finalExam = finalExam;
        this.message = message;
    }
}
