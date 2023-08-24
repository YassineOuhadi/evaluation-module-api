package ma.caftech.sensipro.service.service;

import ma.caftech.sensipro.domain.Course;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface CourseService {

    ResponseEntity<List<Course>> GetAllCourses();

    ResponseEntity<String> deleteQuestion(Map<String, Object> requestMap);
}
