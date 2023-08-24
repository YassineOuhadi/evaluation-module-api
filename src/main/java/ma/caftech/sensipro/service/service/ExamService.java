package ma.caftech.sensipro.service.service;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface ExamService {

    ResponseEntity<Map<String, Object>> beginExam(Map<String, Object> requestMap);

    ResponseEntity<Map<String, Object>> endExam(Integer examId);

    ResponseEntity<Map<String, Object>> canUserTakeExam(Integer campaignId, Integer userId);

    ResponseEntity<Boolean> validateResponse(Map<String, Object> requestMap);
}
