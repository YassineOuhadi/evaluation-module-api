package ma.caftech.sensipro.service.Impl;

import ma.caftech.sensipro.constants.SystemConstants;
import ma.caftech.sensipro.domain.Question;
import ma.caftech.sensipro.dto.CourseDTO;
import ma.caftech.sensipro.repository.CourseRepository;
import ma.caftech.sensipro.domain.Course;
import ma.caftech.sensipro.repository.QuestionRepository;
import ma.caftech.sensipro.service.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import ma.caftech.sensipro.utils.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Override
    public List<CourseDTO> getAllCourses() {
        try {
            List<Course> courses = courseRepository.findAll();
            return courses.stream()
                    .map(CourseDTO::fromCourse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void deleteQuestion(Map<String, Object> requestMap) {
        log.info("Inside delete question {}", requestMap);
        try {
            if (requestMap.containsKey("courseId")) {
                Integer courseId = (Integer) requestMap.get("courseId");
                Optional<Course> optionalCourse = courseRepository.findById(courseId.longValue());
                if (optionalCourse.isPresent()) {
                    Course course = optionalCourse.get();
                    Long questionId = (Long) requestMap.get("questionId");
                    Optional<Question> optionalQuestion = questionRepository.findById(questionId);
                    if (optionalQuestion.isPresent()) {
                        Question question = optionalQuestion.get();
                        course.getQuestions().remove(question);
                        courseRepository.save(course);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
