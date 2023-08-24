package ma.caftech.sensipro;

import ma.caftech.sensipro.repository.QuestionRepository;
import ma.caftech.sensipro.service.Impl.QuestionServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class QueServiceTest {

    @Mock
    private QuestionRepository queDao;

    @InjectMocks
    private QuestionServiceImpl queService;

    @Test
    public void testCreateQue_Success() {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("type", "TRUE_FALSE");
        requestMap.put("title", "Sample Title");
        requestMap.put("code", "ABC001");
        requestMap.put("question", "Is this a true/false question?");
        requestMap.put("correct", true);
        ResponseEntity<String> response = queService.createQuestion(requestMap);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testCreateQue_MissingFields() {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("type", "TRUE_FALSE");
        ResponseEntity<String> response = queService.createQuestion(requestMap);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testCreateQue_InvalidType() {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("type", "INVALID_TYPE"); // Invalid question type
        requestMap.put("title", "Sample Title");
        requestMap.put("code", "ABC001");
        requestMap.put("question", "Is this a true/false question?");
        requestMap.put("correct", true);
        ResponseEntity<String> response = queService.createQuestion(requestMap);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
