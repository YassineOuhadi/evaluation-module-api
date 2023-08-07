package ma.caftech.sensipro.service.Impl;

import ma.caftech.sensipro.repository.CourseRepository;
import ma.caftech.sensipro.domain.Course;
import ma.caftech.sensipro.service.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    CourseRepository courseDao;

    @Override
    public ResponseEntity<List<Course>> GetAllCourses() {
        try {
            return new ResponseEntity<>(courseDao.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
