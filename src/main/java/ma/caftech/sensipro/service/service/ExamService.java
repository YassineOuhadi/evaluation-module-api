package ma.caftech.sensipro.service.service;

import ma.caftech.sensipro.dto.QuestionDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface ExamService {

    List<QuestionDTO> beginExam(Map<String, Object> requestMap);

    Map<String, Object> canUserTakeExam(Long campaignProgressId);

    Map<String, Object> validateResponse(Map<String, Object> requestMap);

    Map<String, Object> endExam(Long campaignProgressId);
}
