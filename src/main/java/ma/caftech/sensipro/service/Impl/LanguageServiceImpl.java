package ma.caftech.sensipro.service.Impl;

import ma.caftech.sensipro.domain.Course;
import ma.caftech.sensipro.dto.CourseDTO;
import ma.caftech.sensipro.dto.LanguageDTO;
import ma.caftech.sensipro.repository.LanguageRepository;
import ma.caftech.sensipro.domain.Language;
import ma.caftech.sensipro.service.service.LanguageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LanguageServiceImpl implements LanguageService {

    @Autowired
    LanguageRepository languageRepository;

    @Override
    public List<LanguageDTO> getLanguages() {
        try {
            List<Language> languages = languageRepository.findAll();
            return languages.stream()
                    .map(LanguageDTO::fromLanguage)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
