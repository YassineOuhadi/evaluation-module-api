package ma.caftech.sensipro.repository;

import ma.caftech.sensipro.domain.AsUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AsUser,Integer> {
}
