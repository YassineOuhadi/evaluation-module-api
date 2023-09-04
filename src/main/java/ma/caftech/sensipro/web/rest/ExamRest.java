package ma.caftech.sensipro.web.rest;

import lombok.extern.slf4j.Slf4j;
import ma.caftech.sensipro.dto.QuestionDTO;
import ma.caftech.sensipro.service.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = "/exam")
public class ExamRest {

    @Autowired
    ExamService examService;

    @PostMapping(path = "/begin")
    public ResponseEntity<List<QuestionDTO>> beginExam(@RequestBody(required = true) Map<String, Object> requestMap) {
        try {
            List<QuestionDTO> questions = examService.beginExam(requestMap);
            if (questions.isEmpty())
                return new ResponseEntity<>(Collections.emptyList(), HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(questions, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("Error occurred: {}", e.getMessage());
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Internal server error occurred: {}", e.getMessage());
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/canTake")
    public ResponseEntity<Map<String, Object>> canUserTakeExam(@RequestParam(required = true) Long campaignProgressId) {
        try {
            Map<String, Object> response = examService.canUserTakeExam(campaignProgressId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("Error occurred: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Internal server error occurred: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/validate")
    public ResponseEntity<Map<String, Object>> validateResponse(@RequestBody(required = true) Map<String, Object> requestMap) {
        try {
            Map<String, Object> validationResponse = examService.validateResponse(requestMap);
            return new ResponseEntity<>(validationResponse, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("Error occurred: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Internal server error occurred: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/endExam")
    public ResponseEntity<Map<String, Object>> endExam(@RequestParam(required = true) Long campaignProgressId) {
        try {
            Map<String, Object> response = examService.endExam(campaignProgressId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.error("Error occurred: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            log.error("Internal server error occurred: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
