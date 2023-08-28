package ma.caftech.sensipro.service.service;

import ma.caftech.sensipro.domain.Language;
import ma.caftech.sensipro.dto.LanguageDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface LanguageService {

    List<LanguageDTO> getLanguages();

}
