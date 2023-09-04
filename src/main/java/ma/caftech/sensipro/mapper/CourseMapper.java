package ma.caftech.sensipro.mapper;


import ma.caftech.sensipro.domain.ChoiceQuestion;
import ma.caftech.sensipro.domain.Course;
import ma.caftech.sensipro.dto.ChoiceQuestionDTO;
import ma.caftech.sensipro.dto.CourseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    Course toCourse(CourseDTO courseDTO);

    CourseDTO tCourseDto(Course course);
}
