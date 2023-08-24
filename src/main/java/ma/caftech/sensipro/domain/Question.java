package ma.caftech.sensipro.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NamedQuery(name = "Question.findByCode", query = "SELECT q FROM Question q WHERE q.code = :code")

@Data
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "question")
@Inheritance(strategy = InheritanceType.JOINED)
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "code", nullable = false)
    private String code;

    //type

    @Column(name = "question", nullable = false)
    private String text;

    @Column(name = "with_timing", nullable = false)
    private boolean isWithTiming;

    @Column(name = "duration", nullable = true, columnDefinition = "integer default 0")
    private Integer duration;

    @Column(name = "correct_answer_tip_text", nullable = false)
    private String correctAnswerTipText;//modify

    @Column(name = "incorrect_answer_tip_text", nullable = false)
    private String incorrectAnswerTipText;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "questions")
    private Set<Course> courses = new HashSet<>();



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_fk", nullable = false)
    @JsonManagedReference // Use this annotation to manage serialization
    @Fetch(FetchMode.JOIN) // Eagerly fetch the associated language
    private Language language;

    // Getter method to return language id and name
    @Transient
    @JsonProperty("language")
    public Map<String, Object> getLanguageInfo() {
        Map<String, Object> languageInfo = new HashMap<>();
        languageInfo.put("id", language.getId());
        languageInfo.put("name", language.getName());
        return languageInfo;
    }
}

