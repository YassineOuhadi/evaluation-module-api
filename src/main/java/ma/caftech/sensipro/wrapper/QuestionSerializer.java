package ma.caftech.sensipro.wrapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import ma.caftech.sensipro.domain.*;

public class QuestionSerializer extends JsonSerializer<Question> {

    @Override
    public void serialize(Question question, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", question.getId());
        jsonGenerator.writeStringField("code", question.getCode());
        jsonGenerator.writeStringField("text", question instanceof FillBlanksQuestion
                ? ((FillBlanksQuestion) question).transformTextWithAsterisks() // Use the transformed text for FillBlanksQuestion
                : question.getText());
        jsonGenerator.writeStringField("correctAnswerTipText", question.getCorrectAnswerTipText());
        jsonGenerator.writeStringField("incorrectAnswerTipText", question.getIncorrectAnswerTipText());

        jsonGenerator.writeBooleanField("isWithTiming", question.isWithTiming());
        jsonGenerator.writeNumberField("duration", question.getDuration());

        jsonGenerator.writeNumberField("duration", question.getDuration());

        if (question instanceof ChoiceQuestion) {
            ChoiceQuestion choiceQuestion = (ChoiceQuestion) question;
            jsonGenerator.writeBooleanField("isMultipleChoice", choiceQuestion.isMultipleChoice());
            jsonGenerator.writeArrayFieldStart("options");
            for (Option option : choiceQuestion.getOptions()) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeNumberField("id", option.getId());
                jsonGenerator.writeStringField("text", option.getText());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }

        if(question instanceof TrueFalseQuestion) {}

        if(question instanceof FillBlanksQuestion) {
            FillBlanksQuestion fillBlanksQuestion = (FillBlanksQuestion) question;
            jsonGenerator.writeBooleanField("isDragWords", fillBlanksQuestion.isDragWords());

            if (fillBlanksQuestion.isDragWords()) {
                List<String> hiddenWords = fillBlanksQuestion.extractBlocksFromText();
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
        for (Course course : question.getCourses()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeNumberField("id", course.getId());
            jsonGenerator.writeStringField("name", course.getName());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();

        Map<String, Object> languageInfo = question.getLanguageInfo();
        jsonGenerator.writeObjectField("language", languageInfo);

        jsonGenerator.writeEndObject();
    }
}
