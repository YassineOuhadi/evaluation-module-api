package ma.caftech.sensipro.domain;

import lombok.Data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

@Data
@Entity
@Table(name = "fill_blanks_question")
public class FillBlanksQuestion extends Question implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "is_drag_words", nullable = true, columnDefinition = "boolean default false")
    private boolean isDragWords;

    public FillBlanksQuestion() {
        setType(QuestionType.FILL_BLANKS);
    }

    public String transformTextWithoutAsterisks() {
        String text = getText();
        String transformedText = text.replaceAll("\\*\\*(.*?)\\*\\*", "$1");
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FillBlanksQuestion question = (FillBlanksQuestion) o;
        return super.getId() != null ? super.getId().equals(question.getId()) : false;
    }

    @Override
    public int hashCode() {
        return super.getId() != null ? super.getId().hashCode() : 0;
    }
}
