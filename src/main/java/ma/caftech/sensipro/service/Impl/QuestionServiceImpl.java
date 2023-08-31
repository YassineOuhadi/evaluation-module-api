package ma.caftech.sensipro.service.Impl;

import ma.caftech.sensipro.constants.SystemConstants;
import ma.caftech.sensipro.dto.*;
import ma.caftech.sensipro.mapper.QuestionMapper;
import ma.caftech.sensipro.repository.LanguageRepository;
import ma.caftech.sensipro.repository.OptionRepository;
import ma.caftech.sensipro.repository.QuestionRepository;
import ma.caftech.sensipro.repository.CourseRepository;
import ma.caftech.sensipro.domain.*;
import ma.caftech.sensipro.service.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.validation.ValidationException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    LanguageRepository languageRepository;

    @Autowired
    OptionRepository optionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public void createQuestion(Map<String, Object> requestMap) throws ValidationException {
        log.info("Inside Create Que {}", requestMap);
        try {
            if (validateQue(requestMap)) {
                Optional<Language> optionalLanguage = languageRepository.findById((Integer) requestMap.get("languageId"));
                if (!optionalLanguage.isPresent())
                    throw new ValidationException("Language id does not exist");

                if (!Objects.isNull(questionRepository.findByCode((String) requestMap.get("code"))))
                    throw new ValidationException("Question code must be unique.");

                QuestionDTO questionDTO = getQuestionDTOFromMap(requestMap, false);
                Question newQuestion = questionMapper.toQuestion(questionDTO); // Use the toQuestion method
                newQuestion.setLanguage(optionalLanguage.get());
                questionRepository.save(newQuestion);
                List<Integer> coursesIds = (List<Integer>) requestMap.get("coursesIds");
                if (coursesIds != null && !coursesIds.isEmpty()) {
                    for (Integer courseId : coursesIds) {
                        Course course = courseRepository.findById(courseId).orElse(null);
                        if (course != null) {
                            course.getQuestions().add(newQuestion);
                            courseRepository.save(course);
                        } else {
                            log.warn("Course with id {} does not exist. Skipping association.", courseId);
                        }
                    }
                }
            } else {
                throw new ValidationException(SystemConstants.INVALID_DATA);
            }
        } catch (Exception e) {
            throw new RuntimeException(SystemConstants.SOMETHING_WENT_WRONG);
        }
    }

    private boolean validateQue(Map<String, Object> requestMap) {
        if (requestMap.containsKey("type") && requestMap.containsKey("code") && requestMap.containsKey("text") && requestMap.containsKey("correctAnswerTip") && requestMap.containsKey("incorrectAnswerTip") && requestMap.containsKey("languageId")) {
            String typeString = (String) requestMap.get("type");
            try {
                Question.QuestionType type = Question.QuestionType.valueOf(typeString);
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

    private QuestionDTO getQuestionDTOFromMap(Map<String, Object> requestMap, boolean isEdit) throws JSONException {
        QuestionDTO dto;
        String typeString = (String) requestMap.get("type");
        Question.QuestionType questionType = Question.QuestionType.valueOf(typeString);
        switch (questionType) {
            case TRUE_FALSE:
                dto = new TrueFalseQuestionDTO();
                ((TrueFalseQuestionDTO) dto).setIsCorrect((Boolean) requestMap.get("isCorrect"));
                break;
            case CHOICE:
                dto = new ChoiceQuestionDTO();
                ((ChoiceQuestionDTO) dto).setIsMultipleChoice((Boolean) requestMap.get("isMultipleChoice"));
                JSONArray optionsArray = new JSONArray((List<Object>) requestMap.get("options"));
                List<Option> options = new ArrayList<>();
                for (int i = 0; i < optionsArray.length(); i++) {
                    JSONObject optionJson = optionsArray.getJSONObject(i);
                    String optionText = optionJson.getString("text");
                    boolean isCorrect = optionJson.getBoolean("isCorrect");
                    if (optionText != null && !optionText.isEmpty()) {
                        Option option = new Option();
                        if(isEdit) option.setId(optionJson.getInt("id")); // Set the option ID if available
                        option.setText(optionText);
                        option.setIsCorrect(isCorrect);
                        options.add(option);
                    }
                }
                if (options.size() < 2) {
                    throw new IllegalArgumentException("A ChoiceQuestion must have at least two options.");
                }
                ((ChoiceQuestionDTO) dto).setOptions(options);
                break;
            case FILL_BLANKS:
                dto = new FillBlanksQuestionDTO();
                ((FillBlanksQuestionDTO) dto).setIsDragWords((Boolean) requestMap.get("isDragWords"));
                break;
            default:
                dto = new QuestionDTO();
                break;
        }
        dto.setCode((String) requestMap.get("code"));
        dto.setText((String) requestMap.get("text"));
        dto.setCorrectAnswerTip((String) requestMap.get("correctAnswerTip"));
        dto.setIncorrectAnswerTip((String) requestMap.get("incorrectAnswerTip"));
        dto.setType(questionType);

        List<Integer> coursesIds = (List<Integer>) requestMap.get("coursesIds");
        Set<CourseDTO> courseDTOs = coursesIds.stream()
                .map(courseId -> CourseDTO.fromCourse(courseRepository.findById(courseId).orElse(null)))
                .collect(Collectors.toSet());
        dto.setCourses(courseDTOs);

        return dto;
    }

    @Override
    public void editQuestion(Map<String, Object> requestMap) {
        log.info("Inside edit question {}", requestMap);
        try {
            if (requestMap.containsKey("id")) {
                Integer questionId = (Integer) requestMap.get("id");
                Optional<Question> optionalQuestion = questionRepository.findById(questionId);
                if (optionalQuestion.isPresent()) {
                    if (validateQue(requestMap)) {
                        Question existingQuestion = optionalQuestion.get();
                        QuestionDTO questionDTO = getQuestionDTOFromMap(requestMap, true);
                        updateQuestionFromDTO(existingQuestion, questionDTO);
                    } else {
                        throw new ValidationException(SystemConstants.INVALID_DATA);
                    }
                } else {
                    throw new ValidationException("Question not found.");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(SystemConstants.SOMETHING_WENT_WRONG);
        }
    }

    private void updateQuestionFromDTO(Question question, QuestionDTO questionDTO) {
        question.setCode(questionDTO.getCode());
        question.setText(questionDTO.getText());
        question.setCorrectAnswerTip(questionDTO.getCorrectAnswerTip());
        question.setIncorrectAnswerTip(questionDTO.getIncorrectAnswerTip());
        if (question instanceof TrueFalseQuestion) {
            ((TrueFalseQuestion) question).setIsCorrect(((TrueFalseQuestionDTO) questionDTO).getIsCorrect());
        }
        else if (question instanceof ChoiceQuestion) {
            ChoiceQuestion choiceQuestion = (ChoiceQuestion) question;
            choiceQuestion.setIsMultipleChoice(((ChoiceQuestionDTO) questionDTO).getIsMultipleChoice());
            for (Option existingOption : new ArrayList<>(choiceQuestion.getOptions())) {
                boolean existsInRequest = ((ChoiceQuestionDTO) questionDTO).getOptions().stream()
                        .anyMatch(optionMap -> optionMap.getId() != null && optionMap.getId().equals(existingOption.getId()));
                if (!existsInRequest) {
                    choiceQuestion.getOptions().remove(existingOption);
                    optionRepository.deleteById(existingOption.getId());
                }
            }
            for (Option optionMap : ((ChoiceQuestionDTO) questionDTO).getOptions()) {
                Integer optionId = optionMap.getId();
                boolean isCorrect = optionMap.getIsCorrect();
                String optionText = optionMap.getText();
                Option existingOption = choiceQuestion.getOptions().stream()
                        .filter(option -> option.getId().equals(optionId))
                        .findFirst()
                        .orElse(null);
                if (existingOption != null) {
                    existingOption.setIsCorrect(isCorrect);
                    existingOption.setText(optionText);
                } else {
                    Option newOption = new Option();
                    newOption.setIsCorrect(isCorrect);
                    newOption.setText(optionText);
                    newOption.setChoiceQuestion(choiceQuestion);
                    choiceQuestion.getOptions().add(newOption);
                }
            }
        } else if (question instanceof FillBlanksQuestion) {
            ((FillBlanksQuestion) question).setIsDragWords(((FillBlanksQuestionDTO) questionDTO).getIsDragWords());
        }

        Set<Course> existingCourses = question.getCourses();
        Set<Course> coursesToAdd = new HashSet<>();
        for (Course existingCourse : existingCourses) {
            boolean existsInDTO = questionDTO.getCourses().stream()
                    .anyMatch(courseDTO -> courseDTO.getId().equals(existingCourse.getId()));
            if (!existsInDTO) {
                existingCourse.getQuestions().remove(question);
                courseRepository.save(existingCourse);
            }
        }
        for (CourseDTO courseDTO : questionDTO.getCourses()) {
            Course course = courseRepository.findById(courseDTO.getId()).orElse(null);
            if (course != null) {
                if (!existingCourses.contains(course)) {
                    course.getQuestions().add(question);
                    coursesToAdd.add(course);
                }
            } else {
                log.warn("Course with id {} does not exist. Skipping association.", courseDTO.getId());
            }
        }
        for (Course courseToAdd : coursesToAdd) {
            courseRepository.save(courseToAdd);
        }
        questionRepository.save(question);
    }

    @Override
    public List<QuestionDTO> getQuestionsByCourse(Integer courseId) {
        log.info("Inside getQuestionsByCourse {}", courseId);
        List<QuestionDTO> questionDTOs = new ArrayList<>();

        try {
            Optional<Course> optionalCourse = courseRepository.findById(courseId);
            if (optionalCourse.isPresent()) {
                Course course = optionalCourse.get();
                questionDTOs.addAll(course.getQuestions().stream()
                        .map(question -> questionMapper.toQuestionDTO(question))
                        .collect(Collectors.toList()));
                Integer count = course.getNumberOfQuestionsInQuiz();
                if (questionDTOs.size() < count) {
                    List<QuestionDTO> additionalQuestionDTOs = getUnusedQuestionDTOs(count - questionDTOs.size());
                    questionDTOs.addAll(additionalQuestionDTOs);
                    Collections.shuffle(questionDTOs);
                } else {
                    Random random = new Random();
                    for (int i = questionDTOs.size() - 1; i > 0; i--) {
                        int j = random.nextInt(i + 1);
                        QuestionDTO temp = questionDTOs.get(i);
                        questionDTOs.set(i, questionDTOs.get(j));
                        questionDTOs.set(j, temp);
                    }
                    questionDTOs = questionDTOs.subList(0, count);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(SystemConstants.SOMETHING_WENT_WRONG);
        }
        return questionDTOs;
    }

    @Override
    public Page<QuestionDTO> getAllQuestions(
            int page, int size, String questionCodeFilter, Integer languageId, Integer courseId, String type,
            String sortAttribute, String sortDirection) {
        log.info("Inside getAllQuestions {}");
        List<Question> allQuestions;
        TypedQuery<Question> typedQuery;
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Question> query = cb.createQuery(Question.class);
        Root<Question> root = query.from(Question.class);
        Predicate predicate = cb.conjunction();

        if (courseId != null && courseId > 0) {
            Subquery<Question> subquery = query.subquery(Question.class);
            Root<Question> subRoot = subquery.from(Question.class);
            Join<Question, Course> subCourseJoin = subRoot.join("courses");
            subquery.select(subRoot);
            subquery.where(cb.equal(subCourseJoin.get("id"), courseId));
            predicate = cb.and(predicate, root.in(subquery));
        }

        if (languageId != null && languageId > 0)
            predicate = cb.and(predicate, cb.equal(root.get("language").get("id"), languageId));

        if (type != null && !type.isEmpty()) {
            try {
                Question.QuestionType enumType = Question.QuestionType.valueOf(type.toUpperCase());
                predicate = cb.and(predicate, cb.equal(root.get("type"), enumType));
            } catch (IllegalArgumentException ex) {
            }
        }

        if (questionCodeFilter != null && !questionCodeFilter.isEmpty())
            predicate = cb.and(predicate, cb.or(
                    cb.like(root.get("code"), "%" + questionCodeFilter + "%"),
                    cb.like(root.get("text"), "%" + questionCodeFilter + "%")
            ));
        query.where(predicate);

        if ("desc".equalsIgnoreCase(sortDirection))
            query.orderBy(cb.desc(root.get(sortAttribute)));
        else
            query.orderBy(cb.asc(root.get(sortAttribute)));

        typedQuery = entityManager.createQuery(query);
        allQuestions = typedQuery.getResultList();

        typedQuery = entityManager.createQuery(query);
        allQuestions = typedQuery.getResultList();
        // Calculate the filtered total count
        Integer totalCount = (Integer) allQuestions.size();
        // Implement pagination on the filtered questions
        int startIdx = Math.min(page * size, allQuestions.size());
        int endIdx = Math.min((page + 1) * size, allQuestions.size());
        List<Question> paginatedQuestions = allQuestions.subList(startIdx, endIdx);

        List<QuestionDTO> questionDTOs = paginatedQuestions.stream()
                .map(question -> questionMapper.toQuestionDTO(question))
                .collect(Collectors.toList());

        // Create Page<QuestionDTO>
        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionDTO> questionDTOPage = new PageImpl<>(questionDTOs, pageable, totalCount);

        return new PageImpl<>(questionDTOs, pageable, totalCount);
    }

    private List<QuestionDTO> getUnusedQuestionDTOs(int count) {
        List<QuestionDTO> unusedQuestionDTOs = new ArrayList<>();
        List<Question> allQuestions = questionRepository.findAll();
        for (Question question : allQuestions) {
            if (question.getCourses().isEmpty()) {
                unusedQuestionDTOs.add(questionMapper.toQuestionDTO(question));
                if (unusedQuestionDTOs.size() >= count) {
                    break;
                }
            }
        }
        Collections.shuffle(unusedQuestionDTOs);
        return unusedQuestionDTOs.subList(0, Math.min(count, unusedQuestionDTOs.size()));
    }

    @Override
    public Question findQuestion(Integer id) {
        log.info("Inside findQuestion {}", id);
        try {
            Optional<Question> question = questionRepository.findById(id);
            return question.orElse(null);
        } catch (Exception e) {
            throw new RuntimeException(SystemConstants.SOMETHING_WENT_WRONG);
        }
    }

    @Override
    public Map<String, Object> validateResponse(Map<String, Object> requestMap) {
        log.info("Inside validate response {}", requestMap);
        try {
            Map<String, Object> response = new HashMap<>();
            if (requestMap.containsKey("questionId")) {
                Integer questionId = (Integer) requestMap.get("questionId");
                Optional<Question> question = questionRepository.findById(questionId);
                if (question.isEmpty())
                    throw new IllegalArgumentException("Question not found.");

                if (question.get() instanceof TrueFalseQuestion) {
                    TrueFalseQuestion trueFalseQuestion = (TrueFalseQuestion) question.get();
                    boolean correctAnswer = trueFalseQuestion.getIsCorrect();
                    Boolean isCorrectValue = (Boolean) requestMap.get("isCorrect");
                    boolean userAnswer = isCorrectValue.booleanValue();
                    response.put("isCorrectAnswer", userAnswer == correctAnswer);
                } else if (question.get() instanceof ChoiceQuestion) {
                    ChoiceQuestion choiceQuestion = (ChoiceQuestion) question.get();
                    List<Option> options = choiceQuestion.getOptions();
                    if (requestMap.containsKey("options")) {
                        boolean isCorrectAnswer = true;
                        JSONArray optionsArray = new JSONArray((List<Object>) requestMap.get("options"));
                        for (int i = 0; i < optionsArray.length(); i++) {
                            JSONObject optionJson = optionsArray.getJSONObject(i);
                            int optionId = optionJson.getInt("id");
                            boolean userAnswer = optionJson.getBoolean("isCorrect");
                            Option actualOption = options.stream().filter(option -> option.getId().equals(optionId)).findFirst().orElse(null);
                            if (actualOption != null) {
                                if (actualOption.getIsCorrect() != userAnswer) {
                                    isCorrectAnswer = false;
                                }
                            }
                        }
                        response.put("isCorrectAnswer", isCorrectAnswer);
                    }
                } else if (question.get() instanceof FillBlanksQuestion) {
                    FillBlanksQuestion fillBlanksQuestion = (FillBlanksQuestion) question.get();
                    List<String> blocks = fillBlanksQuestion.extractBlocksFromText(true);
                    if (requestMap.containsKey("blocks")) {
                        boolean isCorrectAnswer = true;
                        List<Map<String, Object>> userBlocks = (List<Map<String, Object>>) requestMap.get("blocks");
                        if (userBlocks.size() != blocks.size()) {
                            isCorrectAnswer = false;
                        }
                        for (int i = 0; i < userBlocks.size(); i++) {
                            Map<String, Object> userBlock = userBlocks.get(i);
                            int blockNumber = (int) userBlock.get("blockNumber");
                            String blockText = (String) userBlock.get("text");
                            if (blockText.trim().isEmpty()) isCorrectAnswer = false;
                            if (blockNumber > 0 && blockNumber <= blocks.size()) {
                                String originalBlock = blocks.get(blockNumber - 1);
                                if (!isExternalApiRunning()) {
                                    isCorrectAnswer = validateResponseLocally(originalBlock, blockText);
                                } else {
                                    try {
                                        RestTemplate restTemplate = new RestTemplate();
                                        String apiUrl = "http://localhost:5000/compare";
                                        HttpHeaders headers = new HttpHeaders();
                                        headers.setContentType(MediaType.APPLICATION_JSON);
                                        Map<String, Object> requestBody = new HashMap<>();
                                        requestBody.put("user_answer", blockText);
                                        requestBody.put("true_answer", originalBlock);
                                        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                                        ResponseEntity<Map> responseFromFlask = restTemplate.postForEntity(apiUrl, entity, Map.class);
                                        if (responseFromFlask.getStatusCode().is2xxSuccessful()) {
                                            boolean isAnswerCorrect = (boolean) responseFromFlask.getBody().get("is_similar");
                                            if(!isAnswerCorrect) {
                                                isCorrectAnswer = false;
                                            }
                                        }
                                    } catch (Exception e) {
                                        isCorrectAnswer = validateResponseLocally(originalBlock, blockText);
                                    }
                                }
                            }
                        }
                        response.put("isCorrectAnswer", isCorrectAnswer);
                    }
                }
                response.put("responseList", getQuestionAnswer(questionId));
                return response;
            }
            throw new IllegalArgumentException("Invalid requestMap from validateResponse.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteQuestions(List<Integer> ids) {
        log.info("Inside deleteQuestions {}", ids);
        try {
            List<Question> questionsToDelete = questionRepository.findAllById(ids);
            for (Question questionToDelete : questionsToDelete) {
                for (Course course : questionToDelete.getCourses()) {
                    course.getQuestions().remove(questionToDelete);
                    courseRepository.save(course);
                }
                if (questionToDelete instanceof ChoiceQuestion) {
                    for (Option option : ((ChoiceQuestion) questionToDelete).getOptions()) {
                        optionRepository.deleteById(option.getId());
                    }
                }
                questionRepository.delete(questionToDelete);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getQuestionAnswer(Integer idQuestion) {
        List<String> response = new ArrayList<>();
        try {
            Optional<Question> optionalQuestion = questionRepository.findById(idQuestion);
            if (optionalQuestion.isPresent()) {
                Question question = optionalQuestion.get();
                if (question instanceof ChoiceQuestion) {
                    ChoiceQuestion choiceQuestion = (ChoiceQuestion) question;
                    List<Option> options = choiceQuestion.getOptions();
                    for (Option option : options) {
                        if (option.getIsCorrect()) {
                            response.add(option.getText());
                        }
                    }
                } else if (question instanceof TrueFalseQuestion) {
                    TrueFalseQuestion trueFalseQuestion = (TrueFalseQuestion) question;
                    boolean answer = trueFalseQuestion.getIsCorrect();
                    response.add(String.valueOf(answer));
                } else if (question instanceof FillBlanksQuestion) {
                    FillBlanksQuestion fillBlanksQuestion = (FillBlanksQuestion) question;
                    response.add(fillBlanksQuestion.transformTextWithoutAsterisks());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private boolean isExternalApiRunning() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:5000/", String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    private Boolean validateResponseLocally(String trueAnswer, String userAnswer) {
        double similarity = findSimilarity(trueAnswer, userAnswer);
        double similarityThreshold = 0.8;
        if (similarity >= similarityThreshold) return true;
        else return false;
    }

    public static int getLevenshteinDistance(String X, String Y) {
        int m = X.length();
        int n = Y.length();
        int[][] T = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            T[i][0] = i;
        }
        for (int j = 1; j <= n; j++) {
            T[0][j] = j;
        }
        int cost;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                cost = X.charAt(i - 1) == Y.charAt(j - 1) ? 0: 1;
                T[i][j] = Integer.min(Integer.min(T[i - 1][j] + 1, T[i][j - 1] + 1),
                        T[i - 1][j - 1] + cost);
            }
        }
        return T[m][n];
    }

    public static double findSimilarity(String x, String y) {
        if (x == null || y == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }
        double maxLength = Double.max(x.length(), y.length());
        if (maxLength > 0) {
            return (maxLength - getLevenshteinDistance(x, y)) / maxLength;
        }
        return 1.0;
    }
}
