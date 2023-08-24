package ma.caftech.sensipro.repository;

import ma.caftech.sensipro.domain.ExamQuestion;
import ma.caftech.sensipro.domain.id.ExamQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, ExamQuestionId> {
}
