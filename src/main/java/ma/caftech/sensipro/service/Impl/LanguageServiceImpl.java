package ma.caftech.sensipro.service.Impl;

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

@Slf4j
@Service
public class LanguageServiceImpl implements LanguageService {

    @Autowired
    LanguageRepository languageDao;

    @Override
    public ResponseEntity<List<Language>> GetLang() {
        try {
            return new ResponseEntity<>(languageDao.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
