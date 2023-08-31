package ma.caftech.sensipro.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "campaign")
@Data
public class Campaign implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Integer id;

    @Column(columnDefinition = "integer default 11")
    private Integer numberOfQuestionsInExam;

    @Column(columnDefinition = "integer default 3")
    private Integer maxAttemptsAllowed;

    @Column(name = "required_exam_score", columnDefinition = "double default 0.0")
    private double archivingScore; //%

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "rel_campaign__courses",
            joinColumns = @JoinColumn(name = "campaign_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Campaign)) {
            return false;
        }
        return id != null && id.equals(((Campaign) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}