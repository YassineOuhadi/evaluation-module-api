package ma.caftech.sensipro.web.rest;

import ma.caftech.sensipro.constants.SystemConstants;
import ma.caftech.sensipro.service.service.ExamService;
import ma.caftech.sensipro.utils.SystemUtils;
import ma.caftech.sensipro.wrapper.ExamSessionWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/exam")
public class QuizRest {

    @Autowired
    ExamService examService;

    @PostMapping(path = "/begin")
    public ResponseEntity<Map<String, Object>> beginExam(@RequestBody(required = true) Map<String, Object> requestMap) {
        try {
            return examService.beginExam(requestMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(path = "/validate")
    public ResponseEntity<Boolean> validateResponse(@RequestBody(required = true) Map<String, Object> requestMap) {
        try {
            return examService.validateResponse(requestMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(path = "/endExam")
    public ResponseEntity<Map<String, Object>> endExam(@RequestParam(required = true) Integer examId) {
        try {
            return examService.endExam(examId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(path = "/canTake")
    public ResponseEntity<Map<String, Object>> canUserTakeExam(@RequestParam(required = true) Integer campaignId,@RequestParam(required = true) Integer userId) {
        try {
            return examService.canUserTakeExam(campaignId, userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
