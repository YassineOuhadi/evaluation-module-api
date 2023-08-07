package ma.caftech.sensipro.web.rest;

import ma.caftech.sensipro.domain.Language;
import ma.caftech.sensipro.service.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/language")
public class LanguageRest {

    @Autowired
    LanguageService langService;

    @GetMapping(path = "/get")
    public ResponseEntity<List<Language>> GetLang() {
        try {
            return langService.GetLang();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}