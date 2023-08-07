package ma.caftech.sensipro.web.rest;

import ma.caftech.sensipro.domain.Course;
import ma.caftech.sensipro.service.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/course")
public class CourseRest {

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
}
