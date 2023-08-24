package ma.caftech.sensipro.repository;

import ma.caftech.sensipro.domain.CampaignUser;
import ma.caftech.sensipro.domain.id.CampaignUserId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignUserRepository extends JpaRepository<CampaignUser, CampaignUserId> {
}
