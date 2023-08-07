package ma.caftech.sensipro.web.rest;

import ma.caftech.sensipro.constants.SystemConstants;
import ma.caftech.sensipro.service.service.QuestionService;
import ma.caftech.sensipro.utils.SystemUtils;
import ma.caftech.sensipro.wrapper.QuestionWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/question")
public class QuestionRest {

    @Autowired
    QuestionService queService;

    @PostMapping(path = "/new")
    public ResponseEntity<String> CreateQue(@RequestBody(required = true) Map<String, Object> requestMap) {
        try {
            return queService.CreateQue(requestMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SystemUtils.getResponseEntity(SystemConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(path = "/get")
    public ResponseEntity<List<QuestionWrapper>> GetQue(@RequestParam(required = true) Integer count) {
        try {
            return queService.GetQue(count);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(path = "/validate")
    public ResponseEntity<Boolean> validateResponse(@RequestBody(required = true) Map<String, Object> requestMap) {
        try {
            return queService.validateResponse(requestMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(path = "/answer")
    public ResponseEntity<List<String>> getAnswer(@RequestParam(required = true) Integer idQuestion) {
        try {
            return queService.getAnswer(idQuestion);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}