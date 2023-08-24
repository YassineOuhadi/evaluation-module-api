package ma.caftech.sensipro.service.service;

import ma.caftech.sensipro.domain.Question;
import ma.caftech.sensipro.wrapper.QuestionWrapper;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

public interface QuestionService {

    ResponseEntity<String> createQuestion(Map<String,Object> requestMap);

    ResponseEntity<String> editQuestion(Map<String,Object> requestMap);

    ResponseEntity<List<QuestionWrapper>> getQuestionsByCourse(Integer courseId);


    ResponseEntity<Boolean> validateResponse(Map<String, Object> requestMap);

    ResponseEntity<List<String>> getAnswer(Integer idQuestion);

        ResponseEntity<Map<String, Object>> findQuestion(Integer id);

    ResponseEntity<String> deleteQuestion(Integer id);

    ResponseEntity<Page<QuestionWrapper>>getAllQuestions(int page, int size, String questionCodeFilter, Integer languageId, Integer courseId, String type, String sortAttribute, String sortDirection);
}
