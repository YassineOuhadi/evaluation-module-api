package ma.caftech.sensipro.domain;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import javax.persistence.*;
import javax.validation.constraints.AssertTrue;
import java.io.Serializable;
import java.util.List;

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "choice_que")
public class ChoiceQuestion extends Question implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "is_multiple_choice", nullable = false)
    private boolean isMultipleChoice;

    @OneToMany(mappedBy = "choiceQuestion", cascade = CascadeType.ALL)
    private List<Option> options;

    @AssertTrue(message = "A ChoiceQuestion must have at least two options.")
    private boolean isAtLeastTwoOptions() {
        return options != null && options.size() >= 2;
    }
}
