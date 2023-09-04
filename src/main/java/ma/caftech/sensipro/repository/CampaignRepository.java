package ma.caftech.sensipro.repository;

import ma.caftech.sensipro.domain.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign,Long> {
}
