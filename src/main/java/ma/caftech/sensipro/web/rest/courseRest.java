package ma.caftech.sensipro.web.rest;

import ma.caftech.sensipro.constants.SystemConstants;
import ma.caftech.sensipro.domain.Course;
import ma.caftech.sensipro.service.service.CourseService;
import ma.caftech.sensipro.utils.SystemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/course")
public class courseRest {

    @Autowired
    CourseService courseService;

    @GetMapping(path = "/get")
    public ResponseEntity<List<Course>> GetAllCourses() {
        try {
            return courseService.GetAllCourses();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(path = "/deleteQuestion")
    public ResponseEntity<String> deleteQuestion(@RequestBody(required = true) Map<String, Object> requestMap) {
        try {
            return courseService.deleteQuestion(requestMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SystemUtils.getResponseEntity(SystemConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
