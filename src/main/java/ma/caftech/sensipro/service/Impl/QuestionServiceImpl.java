package ma.caftech.sensipro.service.Impl;

import ma.caftech.sensipro.FactoryPattern.QuestionFactory;
import ma.caftech.sensipro.constants.SystemConstants;
import ma.caftech.sensipro.repository.LanguageRepository;
import ma.caftech.sensipro.repository.QuestionRepository;
import ma.caftech.sensipro.repository.CourseRepository;
import ma.caftech.sensipro.domain.*;
import ma.caftech.sensipro.service.service.QuestionService;
import ma.caftech.sensipro.utils.SystemUtils;
import ma.caftech.sensipro.wrapper.QuestionWrapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    CourseRepository courseDao;

    @Autowired
    QuestionRepository queDao;

    @Autowired
    LanguageRepository languageDao;

    @Override
    public ResponseEntity<String> CreateQue(Map<String, Object> requestMap) {
        log.info("Inside Create Que {}", requestMap);
        try {
            if (validateQue(requestMap) && validateDuration(requestMap)) {

                Optional<Language> optionalLanguage = languageDao.findById((Integer) requestMap.get("language_fk"));
                if(!optionalLanguage.isPresent()){
                    return SystemUtils.getResponseEntity("Language id does not exist", HttpStatus.OK);
                }

                if (!Objects.isNull(queDao.findByCode((String) requestMap.get("code")))) {
                    return SystemUtils.getResponseEntity("Question code must be unique.", HttpStatus.BAD_REQUEST);
                }

                Question newQuestion = getQuestionFromMap(requestMap);
                newQuestion.setLanguage(optionalLanguage.get());
                queDao.save(newQuestion);

                List<Integer> coursesIds = (List<Integer>) requestMap.get("coursesIds");

                if (coursesIds != null && !coursesIds.isEmpty()) {
                    for (Integer courseId : coursesIds) {
                        Course course = courseDao.findById(courseId).orElse(null);
                        if (course != null) {
                            course.getQuestions().add(newQuestion);
                            courseDao.save(course);
                        } else {
                            log.warn("Course with id {} does not exist. Skipping association.", courseId);
                        }
                    }
                }

                return SystemUtils.getResponseEntity("Question Created Successfully", HttpStatus.OK);
            } else {
                return SystemUtils.getResponseEntity(SystemConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SystemUtils.getResponseEntity(SystemConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    private boolean validateQue(Map<String, Object> requestMap) {
        if (requestMap.containsKey("type") && requestMap.containsKey("code") && requestMap.containsKey("text") && requestMap.containsKey("isWithTiming") && requestMap.containsKey("correctAnswerTipText") && requestMap.containsKey("incorrectAnswerTipText") && requestMap.containsKey("language_fk")) {
            String typeString = (String) requestMap.get("type");
            try {
                QuestionFactory.QuestionType type = QuestionFactory.QuestionType.valueOf(typeString);
                switch (type) {
                    case TRUE_FALSE:
                        return requestMap.containsKey("isCorrect");
                    case CHOICE:
                        return requestMap.containsKey("isMultipleChoice");
                    case FILL_BLANKS:
                        return requestMap.containsKey("isDragWords");
                    default:
                        return false;
                }
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }

    private boolean validateDuration(Map<String, Object> requestMap) {
        boolean isWithTiming = (boolean) requestMap.get("isWithTiming");
        if (isWithTiming) {
            return requestMap.containsKey("duration") && requestMap.get("duration") != null;
        }
        return true;
    }

    private Question getQuestionFromMap(Map<String, Object> requestMap) throws JSONException, JSONException {
        QuestionFactory.QuestionType type = QuestionFactory.QuestionType.valueOf((String) requestMap.get("type"));
        Question newQuestion = QuestionFactory.createQuestion(type);
        newQuestion.setCode((String) requestMap.get("code"));
        newQuestion.setText((String) requestMap.get("text"));
        newQuestion.setWithTiming((Boolean) requestMap.get("isWithTiming"));
        if(newQuestion.isWithTiming()) newQuestion.setDuration((Integer) requestMap.get("duration"));
        newQuestion.setCorrectAnswerTipText((String) requestMap.get("correctAnswerTipText"));
        newQuestion.setIncorrectAnswerTipText((String) requestMap.get("incorrectAnswerTipText"));

        if (newQuestion instanceof TrueFalseQuestion) {
            TrueFalseQuestion trueFalseQuestion = (TrueFalseQuestion) newQuestion;
            trueFalseQuestion.setCorrect((Boolean) requestMap.get("isCorrect"));
            return trueFalseQuestion;
        } else if (newQuestion instanceof ChoiceQuestion) {
            ChoiceQuestion choiceQuestion = (ChoiceQuestion) newQuestion;
            choiceQuestion.setMultipleChoice((Boolean) requestMap.get("isMultipleChoice"));

            JSONArray optionsArray = new JSONArray((List<Object>) requestMap.get("options"));
            List<Option> options = new ArrayList<>();
            for (int i = 0; i < optionsArray.length(); i++) {
                JSONObject optionJson = optionsArray.getJSONObject(i);
                String optionText = optionJson.getString("text");
                boolean isCorrect = optionJson.getBoolean("isCorrect");

                if (optionText != null && !optionText.isEmpty()) {
                    Option option = new Option();
                    option.setText(optionText);
                    option.setCorrect(isCorrect);
                    option.setChoiceQuestion(choiceQuestion);
                    options.add(option);
                }
            }

            if (options.size() < 2) {
                throw new IllegalArgumentException("A ChoiceQuestion must have at least two options.");
            }

            choiceQuestion.setOptions(options);
            return choiceQuestion;
        } else if (newQuestion instanceof FillBlanksQuestion) {
            FillBlanksQuestion fillBlanksQuestion = (FillBlanksQuestion) newQuestion;
            fillBlanksQuestion.setDragWords((Boolean) requestMap.get("isDragWords"));
            return fillBlanksQuestion;
        }
        return newQuestion;
    }

    @Override
    public ResponseEntity<List<QuestionWrapper>> GetQue(Integer count) {
        try {
            List<QuestionWrapper> allQuestions = new ArrayList<>();
            List<Question> questions = queDao.findAll();

            Random random = new Random();
            for (int i = questions.size() - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                Question temp = questions.get(i);
                questions.set(i, questions.get(j));
                questions.set(j, temp);
            }

            if (count != null && count > 0 && count <= questions.size()) {
                questions = questions.subList(0, count);
            } else if (count != null && count > questions.size()) {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
            }

            for (Question question : questions) {
                QuestionFactory.QuestionType type = getQuestionType(question);
                allQuestions.add(new QuestionWrapper(question, type));
            }

            return new ResponseEntity<>(allQuestions, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private QuestionFactory.QuestionType getQuestionType(Question question) {
        if (question instanceof TrueFalseQuestion) {
            return QuestionFactory.QuestionType.TRUE_FALSE;
        } else if (question instanceof ChoiceQuestion) {
            return QuestionFactory.QuestionType.CHOICE;
        } else if (question instanceof FillBlanksQuestion) {
            return QuestionFactory.QuestionType.FILL_BLANKS;
        }
        return null;
    }

    @Override
    public ResponseEntity<Boolean> validateResponse(Map<String, Object> requestMap) {
        log.info("Inside validate answer {}", requestMap);
        try {
            if (requestMap.containsKey("questionId")) {
                int questionId = Integer.parseInt(String.valueOf( requestMap.get("questionId")));
                Optional<Question> question = queDao.findById(questionId);

                if (question.isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }

                if (question.get() instanceof TrueFalseQuestion) {
                    TrueFalseQuestion trueFalseQuestion = (TrueFalseQuestion) question.get();
                    boolean correctAnswer = trueFalseQuestion.isCorrect();
                    String isCorrectString = (String) requestMap.get("isCorrect");
                    boolean userAnswer = Boolean.parseBoolean(isCorrectString);

                    return new ResponseEntity<>(userAnswer == correctAnswer, HttpStatus.OK);
                }

                else if (question.get() instanceof ChoiceQuestion) {
                    ChoiceQuestion choiceQuestion = (ChoiceQuestion) question.get();
                    List<Option> options = choiceQuestion.getOptions();

                    if (requestMap.containsKey("options")) {
                        JSONArray optionsArray = new JSONArray((List<Object>) requestMap.get("options"));
                        for (int i = 0; i < optionsArray.length(); i++) {
                            JSONObject optionJson = optionsArray.getJSONObject(i);
                            int optionId = optionJson.getInt("id");
                            boolean userAnswer = optionJson.getBoolean("isCorrect");

                            Option actualOption = options.stream().filter(option -> option.getId().equals(optionId)).findFirst().orElse(null);

                            if (actualOption != null) {
                                if (actualOption.isCorrect() != userAnswer) {
                                    return new ResponseEntity<>(false, HttpStatus.OK);
                                }
                            } else {
                                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                            }
                        }
                        return new ResponseEntity<>(true, HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                }

                else if (question.get() instanceof FillBlanksQuestion) {
                    FillBlanksQuestion fillBlanksQuestion = (FillBlanksQuestion) question.get();
                    List<String> blocks = fillBlanksQuestion.extractBlocksFromText();

                    if (requestMap.containsKey("blocks")) {

                        List<Map<String, Object>> userBlocks = (List<Map<String, Object>>) requestMap.get("blocks");

                        if (userBlocks.size() != blocks.size()) {
                            return new ResponseEntity<>(false, HttpStatus.OK);
                        }

                        for (int i = 0; i < userBlocks.size(); i++) {
                            Map<String, Object> userBlock = userBlocks.get(i);
                            int blockNumber = (int) userBlock.get("blockNumber");
                            String blockText = (String) userBlock.get("text");

                            if (blockNumber <= 0 || blockNumber > blocks.size()) {
                                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                            }

                            String originalBlock = blocks.get(blockNumber - 1);

                            try {
                                RestTemplate restTemplate = new RestTemplate();
                                String apiUrl = "http://localhost:5000/compare";
                                HttpHeaders headers = new HttpHeaders();
                                headers.setContentType(MediaType.APPLICATION_JSON);
                                Map<String, Object> requestBody = new HashMap<>();

                                requestBody.put("user_answer", blockText);
                                requestBody.put("true_answer", originalBlock);

                                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                                ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
                                if (response.getStatusCode().is2xxSuccessful()) {
                                    boolean isAnswerCorrect = (boolean) response.getBody().get("is_similar");
                                    if(!isAnswerCorrect) {
                                        return new ResponseEntity<>(false, HttpStatus.OK);
                                    }
                                } else {
                                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }

                        }
                        return new ResponseEntity<>(true, HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                }

                else {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<List<String>> getAnswer(Integer idQuestion) {
        try {
            Optional<Question> optionalQuestion = queDao.findById(idQuestion);

            if (optionalQuestion.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            Question question = optionalQuestion.get();
            if (question instanceof ChoiceQuestion) {
                ChoiceQuestion choiceQuestion = (ChoiceQuestion) question;
                List<Option> options = choiceQuestion.getOptions();
                List<String> answerOptions = new ArrayList<>();
                for (Option option : options) {
                    if (option.isCorrect()) {
                        answerOptions.add(option.getText());
                    }
                }
                List<String> response = answerOptions;
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else if (question instanceof TrueFalseQuestion) {
                TrueFalseQuestion trueFalseQuestion = (TrueFalseQuestion) question;
                boolean answer = trueFalseQuestion.isCorrect();
                List<String> answerText = Collections.singletonList(String.valueOf(answer));
                List<String> response = answerText;
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else if (question instanceof FillBlanksQuestion) {
                FillBlanksQuestion fillBlanksQuestion = (FillBlanksQuestion) question;
                List<String> answerText = Collections.singletonList(fillBlanksQuestion.transformTextWithoutAsterisks());
                List<String> response = answerText;
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}