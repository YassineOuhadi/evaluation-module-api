package ma.caftech.sensipro.repository;

import ma.caftech.sensipro.domain.Language;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageRepository extends JpaRepository<Language,Long> {
}
