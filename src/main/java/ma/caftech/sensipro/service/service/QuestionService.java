package ma.caftech.sensipro.service.service;

import ma.caftech.sensipro.domain.Question;
import ma.caftech.sensipro.dto.QuestionDTO;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

public interface QuestionService {

    void createQuestion(Map<String,Object> requestMap);

    void editQuestion(Map<String,Object> requestMap);

    Question findQuestion(Integer id);

    Page<QuestionDTO> getAllQuestions(int page, int size, String questionCodeFilter, Integer languageId, Integer courseId, String type, String sortAttribute, String sortDirection);

    List<QuestionDTO> getQuestionsByCourse(Integer courseId);

    Map<String, Object> validateResponse(Map<String, Object> requestMap);

    void deleteQuestions(List<Integer> ids);
}
