package ma.caftech.sensipro.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ma.caftech.sensipro.domain.Course;
import ma.caftech.sensipro.domain.Language;

@Data
@NoArgsConstructor
public class LanguageDTO {
    private Integer id;
    private String name;

    public static LanguageDTO fromLanguage(Language language) {
        LanguageDTO dto = new LanguageDTO();
        dto.setId(language.getId());
        dto.setName(language.getName());
        return dto;
    }

}
