package ma.caftech.sensipro.service.service;

import ma.caftech.sensipro.domain.Language;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface LanguageService {

    ResponseEntity<List<Language>> GetLang();

}
