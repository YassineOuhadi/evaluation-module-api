package ma.caftech.sensipro.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "launch_campaign")
@Data
public class LaunchCampaign implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Integer id;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToOne
    private Campaign campaign;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rel_launch_campaign__as_users",
            joinColumns = @JoinColumn(name = "launch_campaign_id"),
            inverseJoinColumns = @JoinColumn(name = "as_users_id")
    )
    private Set<AsUser> asUsers = new HashSet<>();
}
