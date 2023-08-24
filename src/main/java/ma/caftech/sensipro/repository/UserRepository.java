package ma.caftech.sensipro.repository;

import ma.caftech.sensipro.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Integer> {
}
