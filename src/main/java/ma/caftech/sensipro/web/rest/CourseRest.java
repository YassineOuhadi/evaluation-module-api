package ma.caftech.sensipro.web.rest;

import ma.caftech.sensipro.constants.SystemConstants;
import ma.caftech.sensipro.dto.CourseDTO;
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
public class CourseRest {

    @Autowired
    CourseService courseService;

    @GetMapping(path = "/get")
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        try {
            List<CourseDTO> courseDTOs = courseService.getAllCourses();
            if (!courseDTOs.isEmpty()) {
                return new ResponseEntity<>(courseDTOs, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/deleteQuestion")
    public ResponseEntity<String> deleteQuestion(@RequestBody Map<String, Object> requestMap) {
        try {
            courseService.deleteQuestion(requestMap);
            return SystemUtils.getResponseEntity("Questions deleted successfully.", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(SystemConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
