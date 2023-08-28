package ma.caftech.sensipro.service.service;

import ma.caftech.sensipro.domain.Course;
import ma.caftech.sensipro.dto.CourseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface CourseService {

    List<CourseDTO> getAllCourses();

    void deleteQuestion(Map<String, Object> requestMap);
}
