package ma.caftech.sensipro.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@NamedQuery(name = "Question.findByCode", query = "SELECT q FROM Question q WHERE q.code = :code")

@Data
@Entity
@Table(name = "question")
@Inheritance(strategy = InheritanceType.JOINED)
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum QuestionType {
        TRUE_FALSE,
        CHOICE,
        FILL_BLANKS
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "code", nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private QuestionType type;

    @Column(name = "question", nullable = false)
    private String text;

    @Column(name = "correct_answer_tip_text", nullable = false)
    private String correctAnswerTip;

    @Column(name = "incorrect_answer_tip_text", nullable = false)
    private String incorrectAnswerTip;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "questions")
    private Set<Course> courses = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_fk", nullable = false)
    @JsonManagedReference
    @Fetch(FetchMode.JOIN)
    private Language language;
}

