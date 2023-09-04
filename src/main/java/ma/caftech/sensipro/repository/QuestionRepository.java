package ma.caftech.sensipro.repository;

import ma.caftech.sensipro.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question,Long> {

    Question findByCode(@Param("code") String code);
}
