package ma.caftech.sensipro.domain;

import lombok.Data;
import ma.caftech.sensipro.domain.id.CampaignUserId;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Data
@DynamicUpdate
@DynamicInsert
@IdClass(CampaignUserId.class)
public class CampaignUser {
    @Id
    @ManyToOne
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_completed", columnDefinition = "boolean default false")
    private boolean isCompleted;

    @Column(name = "is_archived", columnDefinition = "boolean default false")
    private boolean isArchived;
}

