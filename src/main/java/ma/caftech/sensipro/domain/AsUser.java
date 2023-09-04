package ma.caftech.sensipro.domain;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "as_user")
@Data
public class AsUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Long id;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "asUsers")
    private Set<LaunchCampaign> launchCampaigns = new HashSet<>();
}
