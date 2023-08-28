package ma.caftech.sensipro.repository;

import ma.caftech.sensipro.domain.Campaign;
import ma.caftech.sensipro.domain.FinalExam;
import ma.caftech.sensipro.domain.AsUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExamSessionRepository extends JpaRepository<FinalExam,Integer> {
    Optional<FinalExam> findSession(AsUser user, Campaign campaign, boolean isCompleted);

    Optional<FinalExam> findLastExamSessionByUserAndCampaign(AsUser user, Campaign campaign);
}
