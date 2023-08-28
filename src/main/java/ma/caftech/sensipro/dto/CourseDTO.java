package ma.caftech.sensipro.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ma.caftech.sensipro.domain.*;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class CourseDTO {
    private Integer id;
    private String name;
    public static CourseDTO fromCourse(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        return dto;
    }
}