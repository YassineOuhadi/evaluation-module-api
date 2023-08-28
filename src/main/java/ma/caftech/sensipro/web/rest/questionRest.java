package ma.caftech.sensipro.web.rest;

import ma.caftech.sensipro.constants.SystemConstants;
import ma.caftech.sensipro.domain.Question;
import ma.caftech.sensipro.dto.QuestionDTO;
import ma.caftech.sensipro.service.service.QuestionService;
import ma.caftech.sensipro.utils.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/question")
public class questionRest {

    @Autowired
    QuestionService questionService;

    @PostMapping(path = "/new")
    public ResponseEntity<String> createQuestion(@RequestBody(required = true) Map<String, Object> requestMap) {
        try {
            questionService.createQuestion(requestMap);
            return SystemUtils.getResponseEntity("Question Created Successfully.", HttpStatus.OK);
        } catch (DataAccessException dae) {
            return SystemUtils.getResponseEntity(SystemConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return SystemUtils.getResponseEntity(SystemConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/edit")
    public ResponseEntity<String> editQuestion(@RequestBody(required = true) Map<String, Object> requestMap) {
        try {
            questionService.editQuestion(requestMap);
            return SystemUtils.getResponseEntity("Question Updated Successfully.", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SystemUtils.getResponseEntity(SystemConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(path = "/getByCourse")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByCourse(@RequestParam(required = true) Integer courseId) {
        try {
            List<QuestionDTO> questionDTOs = questionService.getQuestionsByCourse(courseId);
            return new ResponseEntity<>(questionDTOs, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/getAll")
    public ResponseEntity<Page<QuestionDTO>> getAllQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String questionCodeFilter,
            @RequestParam(required = false) Integer languageId,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "id") String sortAttribute,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        try {
            Page<QuestionDTO> questionDTOPage = questionService.getAllQuestions(
                    page, size, questionCodeFilter, languageId, courseId, type, sortAttribute, sortDirection);
            return new ResponseEntity<>(questionDTOPage, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/find")
    public ResponseEntity<Question> findQuestion(@RequestParam(required = true) Integer id) {
        try {
            Question question = questionService.findQuestion(id);
            if (question != null) {
                return new ResponseEntity<>(question, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/validate")
    public ResponseEntity<Boolean> validateResponse(@RequestBody(required = true) Map<String, Object> requestMap) {
        try {
            Boolean validationResult = questionService.validateResponse(requestMap);
            return new ResponseEntity<>(validationResult, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/answer")
    public ResponseEntity<List<String>> getAnswer(@RequestParam(required = true) Integer idQuestion) {
        try {
            List<String> answer = questionService.getAnswer(idQuestion);
            if (!answer.isEmpty()) {
                return new ResponseEntity<>(answer, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/delete")
    public ResponseEntity<String> deleteQuestions(@RequestBody List<Integer> ids) {
        try {
            questionService.deleteQuestions(ids);
            return SystemUtils.getResponseEntity("Questions deleted successfully.", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(SystemConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
