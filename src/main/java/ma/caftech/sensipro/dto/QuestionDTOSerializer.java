package ma.caftech.sensipro.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ma.caftech.sensipro.domain.Option;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class QuestionDTOSerializer extends JsonSerializer<QuestionDTO> {

    @Override
    public void serialize(QuestionDTO questionDTO, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeNumberField("id", questionDTO.getId());
        jsonGenerator.writeStringField("type", String.valueOf(questionDTO.getType()));
        jsonGenerator.writeStringField("code", questionDTO.getCode());
        jsonGenerator.writeStringField("text", questionDTO instanceof FillBlanksQuestionDTO
                ? ((FillBlanksQuestionDTO) questionDTO).transformTextWithAsterisks()
                : questionDTO.getText());
        jsonGenerator.writeStringField("correctAnswerTip", questionDTO.getCorrectAnswerTip());
        jsonGenerator.writeStringField("incorrectAnswerTip", questionDTO.getIncorrectAnswerTip());

        if (questionDTO instanceof ChoiceQuestionDTO) {
            ChoiceQuestionDTO choiceQuestionDTO = (ChoiceQuestionDTO) questionDTO;
            jsonGenerator.writeBooleanField("isMultipleChoice", choiceQuestionDTO.isMultipleChoice());
            jsonGenerator.writeArrayFieldStart("options");
            for (Option option : choiceQuestionDTO.getOptions()) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeNumberField("id", option.getId());
                jsonGenerator.writeStringField("text", option.getText());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }

        else if (questionDTO instanceof FillBlanksQuestionDTO) {
            FillBlanksQuestionDTO fillBlanksQuestionDTO = (FillBlanksQuestionDTO) questionDTO;
            jsonGenerator.writeBooleanField("isDragWords", fillBlanksQuestionDTO.isDragWords());

            if (fillBlanksQuestionDTO.isDragWords()) {
                List<String> hiddenWords = fillBlanksQuestionDTO.extractBlocksFromText();
                jsonGenerator.writeArrayFieldStart("hiddenWords");
                for (String hiddenWord : hiddenWords) {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField("word", hiddenWord);
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();
            }
        }

        jsonGenerator.writeArrayFieldStart("courses");
        for (CourseDTO course : questionDTO.getCourses()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeNumberField("id", course.getId());
            jsonGenerator.writeStringField("name", course.getName());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        Map<String, Object> languageInfo = questionDTO.getLanguageInfo();
        jsonGenerator.writeObjectField("language", languageInfo);

        jsonGenerator.writeEndObject();
    }
}
