package ma.caftech.sensipro.service.service;

import ma.caftech.sensipro.wrapper.QuestionWrapper;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

public interface QuestionService {

    ResponseEntity<String> CreateQue(Map<String,Object> requestMap);

    ResponseEntity<List<QuestionWrapper>> GetQue(Integer count);


    ResponseEntity<Boolean> validateResponse(Map<String, Object> requestMap);

    ResponseEntity<List<String>> getAnswer(Integer idQuestion);
}
