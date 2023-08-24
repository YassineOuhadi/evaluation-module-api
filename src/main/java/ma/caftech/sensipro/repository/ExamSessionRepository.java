package ma.caftech.sensipro.repository;

import ma.caftech.sensipro.domain.Campaign;
import ma.caftech.sensipro.domain.FinalExam;
import ma.caftech.sensipro.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamSessionRepository extends JpaRepository<FinalExam,Integer> {
    Optional<FinalExam> findSession(User user, Campaign campaign, boolean isCompleted);

    Optional<FinalExam> findLastExamSessionByUserAndCampaign(User user, Campaign campaign);
}
