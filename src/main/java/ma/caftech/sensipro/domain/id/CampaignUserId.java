package ma.caftech.sensipro.domain.id;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class CampaignUserId implements Serializable {
    private Integer campaign;
    private Integer user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CampaignUserId)) return false;
        CampaignUserId that = (CampaignUserId) o;
        return Objects.equals(campaign, that.campaign) &&
                Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(campaign,user);
    }
}
