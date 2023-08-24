package ma.caftech.sensipro.web.rest;

import ma.caftech.sensipro.constants.SystemConstants;
import ma.caftech.sensipro.domain.Question;
import ma.caftech.sensipro.service.service.QuestionService;
import ma.caftech.sensipro.utils.SystemUtils;
import ma.caftech.sensipro.wrapper.QuestionWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/question")
public class questionRest {

    @Autowired
    QuestionService queService;

    @PostMapping(path = "/new")
    public ResponseEntity<String> createQuestion(@RequestBody(required = true) Map<String, Object> requestMap) {
        try {
            return queService.createQuestion(requestMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SystemUtils.getResponseEntity(SystemConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(path = "/edit")
    public ResponseEntity<String> editQuestion(@RequestBody(required = true) Map<String, Object> requestMap) {
        try {
            return queService.editQuestion(requestMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SystemUtils.getResponseEntity(SystemConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(path = "/delete")
    public ResponseEntity<String> deleteQuestion(@RequestParam(required = true) Integer id) {
        try {
            return queService.deleteQuestion(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SystemUtils.getResponseEntity(SystemConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }



    @GetMapping(path = "/getByCourse")
    public ResponseEntity<List<QuestionWrapper>> getQuestionsByCourse(@RequestParam(required = true) Integer courseId) {
        try {
            return queService.getQuestionsByCourse(courseId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(path = "/getAll")
    public ResponseEntity<Page<QuestionWrapper>> getAllQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String questionCodeFilter,
            @RequestParam(required = false) Integer languageId,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "id") String sortAttribute,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        try {
            return queService.getAllQuestions(page, size, questionCodeFilter, languageId, courseId, type, sortAttribute, sortDirection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(path = "/find")
    public ResponseEntity<Map<String, Object>> findQuestion(@RequestParam(required = true) Integer id) {
        try {
            return queService.findQuestion(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
