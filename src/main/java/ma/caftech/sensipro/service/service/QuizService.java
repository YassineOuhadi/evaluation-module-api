package ma.caftech.sensipro.service.service;

import ma.caftech.sensipro.dto.QuestionDTO;

import java.util.List;
import java.util.Map;

public interface QuizService {

    List<QuestionDTO> beginExam(Map<String, Object> requestMap);

    Map<String, Object> canUserTakeExam(Long campaignProgressId);

    Map<String, Object> validateResponse(Map<String, Object> requestMap);

    Map<String, Object> endExam(Long campaignProgressId);
}
