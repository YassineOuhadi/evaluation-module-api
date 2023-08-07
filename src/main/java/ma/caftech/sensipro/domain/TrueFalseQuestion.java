package ma.caftech.sensipro.domain;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "true_false_que")
public class TrueFalseQuestion extends Question implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;
}
