package ma.caftech.sensipro.service.Impl;

import ma.caftech.sensipro.FactoryPattern.QuestionFactory;
import ma.caftech.sensipro.constants.SystemConstants;
import ma.caftech.sensipro.repository.LanguageRepository;
import ma.caftech.sensipro.repository.OptionRepository;
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
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    CourseRepository courseDao;

    @Autowired
    QuestionRepository queDao;

    @Autowired
    LanguageRepository languageDao;

    @Autowired
    OptionRepository optionRepository;


    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public ResponseEntity<String> createQuestion(Map<String, Object> requestMap) {
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
    public ResponseEntity<String> editQuestion(Map<String, Object> requestMap) {
        log.info("Inside edit question {}", requestMap);
        try {
            if(requestMap.containsKey("id")) {
                Integer questionId = (Integer) requestMap.get("id");
                Optional<Question> optionalQuestion = queDao.findById(questionId);

                if (optionalQuestion.isPresent()) {
                    Question existingQuestion = optionalQuestion.get();

                    if (validateQue(requestMap) && validateDuration(requestMap)) {
                        updateQuestionFromMap(existingQuestion, requestMap);
                        handleAssociations(existingQuestion, requestMap);
                        return SystemUtils.getResponseEntity("Question Updated Successfully", HttpStatus.OK);
                    } else {
                        return SystemUtils.getResponseEntity(SystemConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
                    }

                } else {
                    return SystemUtils.getResponseEntity("Question not found", HttpStatus.NOT_FOUND);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SystemUtils.getResponseEntity(SystemConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void updateQuestionFromMap(Question question, Map<String, Object> requestMap) throws JSONException {
        question.setCode((String) requestMap.get("code"));
        question.setText((String) requestMap.get("text"));
        question.setWithTiming((Boolean) requestMap.get("isWithTiming"));
        if (question.isWithTiming()) question.setDuration((Integer) requestMap.get("duration"));
        question.setCorrectAnswerTipText((String) requestMap.get("correctAnswerTipText"));
        question.setIncorrectAnswerTipText((String) requestMap.get("incorrectAnswerTipText"));

        Optional<Language> optionalLanguage = languageDao.findById((Integer) requestMap.get("language_fk"));
        optionalLanguage.ifPresent(language -> question.setLanguage(language));

        if (question instanceof TrueFalseQuestion) {
            TrueFalseQuestion trueFalseQuestion = (TrueFalseQuestion) question;
            trueFalseQuestion.setCorrect((Boolean) requestMap.get("isCorrect"));
        }
        else if (question instanceof ChoiceQuestion) {
            ChoiceQuestion choiceQuestion = (ChoiceQuestion) question;
            choiceQuestion.setMultipleChoice((Boolean) requestMap.get("isMultipleChoice"));
            List<Map<String, Object>> optionsArray = (List<Map<String, Object>>) requestMap.get("options");
            for (Option existingOption : new ArrayList<>(choiceQuestion.getOptions())) {
                boolean existsInRequest = optionsArray.stream()
                        .anyMatch(optionMap -> existingOption.getId().equals(optionMap.get("id")));
                if (!existsInRequest) {
                    choiceQuestion.getOptions().remove(existingOption);
                    optionRepository.deleteById(existingOption.getId());
                }
            }
            for (Map<String, Object> optionMap : optionsArray) {
                Integer optionId = (Integer) optionMap.get("id");
                boolean isCorrect = (boolean) optionMap.get("isCorrect");
                String optionText = (String) optionMap.get("text");
                Option existingOption = choiceQuestion.getOptions().stream()
                        .filter(option -> option.getId().equals(optionId))
                        .findFirst()
                        .orElse(null);
                if (existingOption != null) {
                    existingOption.setCorrect(isCorrect);
                    existingOption.setText(optionText);
                } else {
                    Option newOption = new Option();
                    newOption.setCorrect(isCorrect);
                    newOption.setText(optionText);
                    newOption.setChoiceQuestion(choiceQuestion);
                    choiceQuestion.getOptions().add(newOption);
                }
            }
        }
        else if (question instanceof FillBlanksQuestion) {
            FillBlanksQuestion fillBlanksQuestion = (FillBlanksQuestion) question;
            fillBlanksQuestion.setDragWords((Boolean) requestMap.get("isDragWords"));
        }
        queDao.save(question);
    }

    private void handleAssociations(Question question, Map<String, Object> requestMap) {
        List<Integer> coursesIdsInRequest = (List<Integer>) requestMap.get("coursesIds");
        Set<Course> existingCourses = question.getCourses();
        Set<Course> coursesToAdd = new HashSet<>();
        for (Course existingCourse : existingCourses) {
            if (!coursesIdsInRequest.contains(existingCourse.getId())) {
                existingCourse.getQuestions().remove(question);
                courseDao.save(existingCourse);
            }
        }
        for (Integer courseId : coursesIdsInRequest) {
            Course course = courseDao.findById(courseId).orElse(null);
            if (course != null) {
                if (!existingCourses.contains(course)) {
                    course.getQuestions().add(question);
                    coursesToAdd.add(course);
                }
            } else {
                log.warn("Course with id {} does not exist. Skipping association.", courseId);
            }
        }
        for (Course courseToAdd : coursesToAdd) {
            courseDao.save(courseToAdd);
        }
    }

    @Override
    public ResponseEntity<List<QuestionWrapper>> getQuestionsByCourse(Integer courseId) {
        try {
            List<QuestionWrapper> allQuestions = new ArrayList<>();
            //List<Question> questions = queDao.findAll();

            Optional<Course> optionalCourse = courseDao.findById(courseId);
            if(optionalCourse.isEmpty()) return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);

            Course course = optionalCourse.get();
            List<Question> questions = new ArrayList<>(course.getQuestions());
            Integer count = course.getQuizNumberQuestions();

            if (questions.size() < count) {
                List<Question> additionalQuestions = new ArrayList<>();
                additionalQuestions = getUnusedQuestions(count - questions.size());
                questions.addAll(additionalQuestions);
                Collections.shuffle(questions);
            } else {
                Random random = new Random();
                for (int i = questions.size() - 1; i > 0; i--) {
                    int j = random.nextInt(i + 1);
                    Question temp = questions.get(i);
                    questions.set(i, questions.get(j));
                    questions.set(j, temp);
                }
                questions = questions.subList(0, count);
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


    // page a referee
    @Override
    public ResponseEntity<Page<QuestionWrapper>> getAllQuestions(
            int page, int size,String questionCodeFilter, Integer languageId, Integer courseId, String type,
            String sortAttribute, String sortDirection) {
        try {
            System.out.println(sortAttribute);
            System.out.println(sortDirection);

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Question> query = cb.createQuery(Question.class);
            Root<Question> root = query.from(Question.class);

            // Apply filtering conditions based on courseId, languageId, and type and code and question text
            Predicate predicate = cb.conjunction();

            if (courseId != null && courseId > 0) {
                Subquery<Question> subquery = query.subquery(Question.class);
                Root<Question> subRoot = subquery.from(Question.class);
                Join<Question, Course> subCourseJoin = subRoot.join("courses");
                subquery.select(subRoot);
                subquery.where(cb.equal(subCourseJoin.get("id"), courseId));

                predicate = cb.and(predicate, root.in(subquery));
            }

            if (languageId != null && languageId > 0) {
                predicate = cb.and(predicate, cb.equal(root.get("language").get("id"), languageId));
            }

            if (questionCodeFilter != null && !questionCodeFilter.isEmpty()) {
                predicate = cb.and(predicate, cb.or(
                        cb.like(root.get("code"), "%" + questionCodeFilter + "%"),
                        cb.like(root.get("text"), "%" + questionCodeFilter + "%")
                ));
            }

            query.where(predicate);

            // Apply sorting based on sortAttribute and sortDirection
            List<Question> allQuestions;
            TypedQuery<Question> typedQuery;

            if ("type".equalsIgnoreCase(sortAttribute)) {
                typedQuery = entityManager.createQuery(query);
                allQuestions=orderQuestionsByType(typedQuery.getResultList(), sortDirection);

            } else {
                if ("desc".equalsIgnoreCase(sortDirection)) {
                    query.orderBy(cb.desc(root.get(sortAttribute)));
                } else {
                    query.orderBy(cb.asc(root.get(sortAttribute)));
                }
                typedQuery = entityManager.createQuery(query);
                // Get all questions without pagination
                allQuestions = typedQuery.getResultList();
            }

            // Filter questions based on the determined type
            List<Question> filteredQuestions = allQuestions.stream()
                    .filter(question -> {
                        if ("CHOICE".equalsIgnoreCase(type)) {
                            return question instanceof ChoiceQuestion;
                        } else if ("FILL_BLANKS".equalsIgnoreCase(type)) {
                            return question instanceof FillBlanksQuestion;
                        } else if ("TRUE_FALSE".equalsIgnoreCase(type)) {
                            return question instanceof TrueFalseQuestion;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());

            // Calculate the filtered total count
            Integer totalCount = (Integer) filteredQuestions.size();

            // Implement pagination on the filtered questions
            int startIdx = Math.min(page * size, filteredQuestions.size());
            int endIdx = Math.min((page + 1) * size, filteredQuestions.size());

            List<Question> paginatedQuestions = filteredQuestions.subList(startIdx, endIdx);

            // Convert questions to QuestionWrapper
            List<QuestionWrapper> questionWrappers = paginatedQuestions.stream()
                    .map(question -> new QuestionWrapper(question, getQuestionType(question)))
                    .collect(Collectors.toList());

            // Create Page<QuestionWrapper>
            Pageable pageable = PageRequest.of(page, size);
            Page<QuestionWrapper> questionWrapperPage = new PageImpl<>(questionWrappers, pageable, totalCount);

            return new ResponseEntity<>(questionWrapperPage, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private Long getTotalQuestionCount(Predicate predicate) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        countQuery.select(cb.count(countQuery.from(Question.class))).where(predicate);
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<Question> getUnusedQuestions(int count) {
        List<Question> unusedQuestions = new ArrayList<>();
        List<Question> allQuestions = queDao.findAll();

        for (Question question : allQuestions) {
            if (question.getCourses().isEmpty()) {
                unusedQuestions.add(question);
                if (unusedQuestions.size() >= count) {
                    break;
                }
            }
        }

        Collections.shuffle(unusedQuestions);
        return unusedQuestions.subList(0, Math.min(count, unusedQuestions.size()));
    }

    @Override
    public ResponseEntity<Map<String, Object>> findQuestion(Integer id) {
        try {
            Optional<Question> question = queDao.findById(id);

            if (question.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            QuestionFactory.QuestionType type = getQuestionType(question.get());

            Map<String, Object> response = new HashMap<>();
            response.put("question", question.get());
            response.put("type", type.toString()); // Convert the enum to a string

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteQuestion(Integer id) {
        try {
            Optional<Question> optionalQuestion = queDao.findById(id);
            if (optionalQuestion.isPresent()) {
                Question questionToDelete = optionalQuestion.get();

                for (Course course : questionToDelete.getCourses()) {
                    course.getQuestions().remove(questionToDelete);
                    courseDao.save(course);
                }

                if (questionToDelete instanceof ChoiceQuestion) {
                    for (Option option : ((ChoiceQuestion) questionToDelete).getOptions()) {
                        optionRepository.deleteById(option.getId());
                    }
                }

                queDao.delete(questionToDelete);

                return SystemUtils.getResponseEntity("Question deleted successfully", HttpStatus.OK);
            } else {
                return SystemUtils.getResponseEntity("Question not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return SystemUtils.getResponseEntity(SystemConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
                Integer questionId = (Integer) requestMap.get("questionId");
                Optional<Question> question = queDao.findById(questionId);

                if (question.isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }

                if (question.get() instanceof TrueFalseQuestion) {
                    TrueFalseQuestion trueFalseQuestion = (TrueFalseQuestion) question.get();
                    boolean correctAnswer = trueFalseQuestion.isCorrect();
                    Boolean isCorrectValue = (Boolean) requestMap.get("isCorrect");
                    boolean userAnswer = isCorrectValue.booleanValue(); // Convert Boolean to boolean primitive

                    //boolean userAnswer = Boolean.parseBoolean(isCorrectString);

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
                            if (blockText.trim().isEmpty()) return new ResponseEntity<>(false, HttpStatus.OK);

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

    private Boolean isResponseCorrect(Map<String, Object> requestMap) throws JSONException {
        if (requestMap.containsKey("questionId")) {
            Optional<Question> question = queDao.findById((Integer) requestMap.get("questionId"));

            if(question.isPresent()) {

                if (question.get() instanceof TrueFalseQuestion) {
                    if(requestMap.containsKey("isCorrect")) {
                        TrueFalseQuestion trueFalseQuestion = (TrueFalseQuestion) question.get();
                        boolean correctAnswer = trueFalseQuestion.isCorrect();
                        boolean userAnswer = Boolean.parseBoolean((String) requestMap.get("isCorrect"));
                        return userAnswer == correctAnswer;
                    }
                }

                else if (question.get() instanceof ChoiceQuestion) {
                    if(requestMap.containsKey("options")) {
                        ChoiceQuestion choiceQuestion = (ChoiceQuestion) question.get();
                        List<Option> options = choiceQuestion.getOptions();
                        JSONArray optionsArray = new JSONArray((List<Object>) requestMap.get("options"));
                        for (int i = 0; i < optionsArray.length(); i++) {
                            JSONObject optionJson = optionsArray.getJSONObject(i);
                            int optionId = optionJson.getInt("id");
                            boolean userAnswer = optionJson.getBoolean("isCorrect");
                            Option actualOption = options.stream().filter(option -> option.getId().equals(optionId)).findFirst().orElse(null);
                            if (actualOption != null) {
                                if (actualOption.isCorrect() != userAnswer) {
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                }

                else if (question.get() instanceof FillBlanksQuestion) {
                    if(requestMap.containsKey("blocks")) {
                        FillBlanksQuestion fillBlanksQuestion = (FillBlanksQuestion) question.get();
                        List<String> blocks = fillBlanksQuestion.extractBlocksFromText();

                        if (requestMap.containsKey("blocks")) {
                            List<Map<String, Object>> userBlocks = (List<Map<String, Object>>) requestMap.get("blocks");
                            if (userBlocks.size() != blocks.size()) return false;
                            for (int i = 0; i < userBlocks.size(); i++) {
                                Map<String, Object> userBlock = userBlocks.get(i);
                                int blockNumber = (int) userBlock.get("blockNumber");
                                String blockText = (String) userBlock.get("text");
                                if(blockNumber > 0 || blockNumber <= blocks.size()) {
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
                                                return false;
                                            }
                                        }
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                            }
                            return true;
                        }
                    }
                }

            }
        }
        return null;
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

    private List<Question> orderQuestionsByType(List<Question> questions,  String sortDirection) {
        List<Question> orderedQuestions = new ArrayList<>();
        List<Question> choiceQuestions = new ArrayList<>();
        List<Question> fillBlanksQuestions = new ArrayList<>();
        List<Question> trueFalseQuestions = new ArrayList<>();

        for (Question question : questions) {
            if (question instanceof ChoiceQuestion) {
                choiceQuestions.add(question);
            } else if (question instanceof FillBlanksQuestion) {
                fillBlanksQuestions.add(question);
            } else if (question instanceof TrueFalseQuestion) {
                trueFalseQuestions.add(question);
            }
        }

        if ("asc".equalsIgnoreCase(sortDirection)) {
            orderedQuestions.addAll(choiceQuestions);
            orderedQuestions.addAll(fillBlanksQuestions);
            orderedQuestions.addAll(trueFalseQuestions);
        } else if ("desc".equalsIgnoreCase(sortDirection)) {
            orderedQuestions.addAll(trueFalseQuestions);
            orderedQuestions.addAll(fillBlanksQuestions);
            orderedQuestions.addAll(choiceQuestions);
        }
        return orderedQuestions;
    }
}
