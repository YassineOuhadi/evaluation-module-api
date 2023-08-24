package ma.caftech.sensipro.service.Impl;

import ma.caftech.sensipro.constants.SystemConstants;
import ma.caftech.sensipro.domain.Question;
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

@Slf4j
@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    CourseRepository courseDao;

    @Autowired
    QuestionRepository questionRepository;

    @Override
    public ResponseEntity<List<Course>> GetAllCourses() {
        try {
            return new ResponseEntity<>(courseDao.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteQuestion(Map<String, Object> requestMap) {
        log.info("Inside delete question {}", requestMap);
        try {
            if(requestMap.containsKey("courseId")) {
                Integer courseId = (Integer) requestMap.get("courseId");
                Optional<Course> optionalCourse = courseDao.findById(courseId);
                if(optionalCourse.isPresent()) {
                    Course course = optionalCourse.get();

                    Integer questionId = (Integer) requestMap.get("questionId");
                    Optional<Question> optionalQuestion = questionRepository.findById(questionId);
                    if(optionalQuestion.isPresent()) {
                        Question question = optionalQuestion.get();

                        course.getQuestions().remove(question);
                        courseDao.save(course);

                        return SystemUtils.getResponseEntity("Question deleted successfully", HttpStatus.OK);


                    } else {
                        return SystemUtils.getResponseEntity("Question not found", HttpStatus.NOT_FOUND);
                    }
                } else {
                    return SystemUtils.getResponseEntity("Course not found", HttpStatus.NOT_FOUND);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return SystemUtils.getResponseEntity(SystemConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
