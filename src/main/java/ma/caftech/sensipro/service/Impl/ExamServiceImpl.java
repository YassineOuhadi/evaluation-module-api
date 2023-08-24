package ma.caftech.sensipro.service.Impl;

import lombok.extern.slf4j.Slf4j;
import ma.caftech.sensipro.FactoryPattern.QuestionFactory;
import ma.caftech.sensipro.domain.*;
import ma.caftech.sensipro.domain.id.CampaignUserId;
import ma.caftech.sensipro.domain.id.ExamQuestionId;
import ma.caftech.sensipro.repository.*;
import ma.caftech.sensipro.service.service.ExamService;
import ma.caftech.sensipro.wrapper.QuestionWrapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ma.caftech.sensipro.constants.SystemConstants;
import ma.caftech.sensipro.utils.SystemUtils;
import java.time.LocalDateTime;
import java.util.*;
import java.time.Duration;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    ExamSessionRepository examSessionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CampaignRepository campaignRepository;

    @Autowired
    CampaignUserRepository campaignUserRepository;

    @Autowired
    ExamQuestionRepository examQuestionRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Override
    public ResponseEntity<Map<String, Object>> beginExam(Map<String, Object> requestMap) {
        Integer userId = (Integer) requestMap.get("userId");
        Integer campaignId = (Integer) requestMap.get("campaignId");
        //String machineName = (String) requestMap.get("machineName");
        //String browser = (String) requestMap.get("browser");

        Optional<User> optionalUser = userRepository.findById(userId);
        Optional<Campaign> optionalCampaign = campaignRepository.findById(campaignId);

        if (optionalUser.isEmpty() || optionalCampaign.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "User or campaign not found."));
        }

        User user = optionalUser.get();
        Campaign campaign = optionalCampaign.get();

        CampaignUserId campaignUserId = new CampaignUserId();
        campaignUserId.setCampaign(campaign.getId());
        campaignUserId.setUser(user.getId());

        Optional<CampaignUser> optionalCampaignUser = campaignUserRepository.findById(campaignUserId);
        if (!optionalCampaignUser.isPresent()) return ResponseEntity.badRequest().body(Collections.singletonMap("error", "User not active in this campaign."));

        CampaignUser campaignUser = optionalCampaignUser.get();
        if(campaignUser.isArchived()) return ResponseEntity.badRequest().body(Collections.singletonMap("error", "User has already archived this campaign."));
        if(!campaignUser.isCompleted()) return ResponseEntity.badRequest().body(Collections.singletonMap("error", "User has not completed the campaign."));

        // We are certain that the user has not archived the final exam
        Optional<FinalExam> existingSession = examSessionRepository.findSession(user, campaign, false);
        FinalExam quizSession;

        if (existingSession.isPresent()) {
            quizSession = existingSession.get();

            LocalDateTime now = LocalDateTime.now();
            Duration durationSinceStart = Duration.between(quizSession.getStartTime(), now);
            if (durationSinceStart.toMinutes() >= campaign.getTestOpenDuration()) {
                markQuizSessionAsCompleted(quizSession);
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Quiz session has expired."));
            }
            /*
            int tentativeCount = quizSession.getTentatives().size();
            if (tentativeCount >= campaign.getExamNumberTentatives()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "The maximum number of tentatives allowed is " + campaign.getExamNumberTentatives()));
            }
            Tentative lastTentative = null;
            if (tentativeCount > 0) {
                lastTentative = quizSession.getTentatives().get(tentativeCount - 1);
                if (lastTentative.isActivate()) {
                    return ResponseEntity.badRequest().body(Collections.singletonMap("error", "An active tentative already exists."));
                }
            }
            */
        } else {
            Optional<FinalExam> lastExamSession = examSessionRepository.findLastExamSessionByUserAndCampaign(user, campaign);
            if(lastExamSession.isPresent()) {
                FinalExam lastExam = lastExamSession.get();
                LocalDateTime startTime = lastExam.getStartTime();
                LocalDateTime now = LocalDateTime.now();
                Duration timeSinceStart = Duration.between(startTime, now);
                int retryTestDurationHours = campaign.getRetryTestDuration();
                Duration maxAllowedDuration = Duration.ofHours(retryTestDurationHours);
                if (timeSinceStart.compareTo(maxAllowedDuration) < 0) {
                    return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Retry test is not allowed yet."));
                }
            }

            quizSession = new FinalExam();
            quizSession.setUser(user);
            quizSession.setCampaign(campaign);
            quizSession.setStartTime(LocalDateTime.now());
            examSessionRepository.save(quizSession);
        }

        /*
        Tentative newTentative = new Tentative();
        newTentative.setFinalExam(quizSession);
        newTentative.setStartTime(LocalDateTime.now());
        newTentative.setMachineName(machineName);
        newTentative.setBrowser(browser);
        tentativeRepository.save(newTentative);
        quizSession.getTentatives().add(newTentative);
        examSessionRepository.save(quizSession);
         */

        LocalDateTime endDate = quizSession.getStartTime().plusMinutes(campaign.getTestOpenDuration());
        List<QuestionWrapper> questions = getQuestions(quizSession);
        if (questions.isEmpty()) {

            // make quiz final au cas tous les question snumber fait
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "No questions available."));
        }
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", quizSession.getId());
        //response.put("tentativeId", newTentative.getId());
        response.put("examSessionEndDate", endDate);
        response.put("questions", questions);  // Add the list of questions to the response

        // return aussi coutnt of questions and count questions deja fait

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Boolean> validateResponse(Map<String, Object> requestMap) {
        log.info("Inside validate response {}", requestMap);
        try {
            if (requestMap.containsKey("questionId") && requestMap.containsKey("sessionId")) {
                Optional<Question> questionOptional = questionRepository.findById((Integer) requestMap.get("questionId"));
                Optional<FinalExam> finalExamOptional = examSessionRepository.findById((Integer) requestMap.get("sessionId"));
                if (questionOptional.isEmpty() || finalExamOptional.isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }

                Question question = questionOptional.get();
                FinalExam finalExam = finalExamOptional.get();




                if(!finalExam.isCompleted()) {

                    ExamQuestion examQuestion = new ExamQuestion();
                    examQuestion.setExam(finalExam);
                    examQuestion.setQuestion(question);
                    examQuestion.setResponse(null);

                    //finalExam.getUsedQuestions().add(question);
                    finalExam.setEndTime(LocalDateTime.now()); // last activity
                    boolean isCorrect = isResponseCorrect(requestMap);
                    examQuestion.setCorrect(isCorrect);

                    int totalCorrectAnswers = (int) finalExam.getExamQuestions().stream()
                            .filter(ExamQuestion::isCorrect)
                            .count();
                    if (isCorrect) totalCorrectAnswers++;
                    double scorePercentage = (double) totalCorrectAnswers / finalExam.getCampaign().getExamNumberQuestions() * 100;
                    finalExam.setExamScore(scorePercentage);

                    examQuestionRepository.save(examQuestion);
                    examSessionRepository.save(finalExam);
                    //tentativeRepository.save(tentative);
                    return new ResponseEntity<>(isCorrect, HttpStatus.OK);
                }
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> endExam(Integer examId) {
        try {
            log.info("Inside end exam {}", examId);
            Optional<FinalExam> optionalExamSession = examSessionRepository.findById(examId);
            if (optionalExamSession.isPresent()) {
                FinalExam examSession = optionalExamSession.get();
                markQuizSessionAsCompleted(examSession);
                CampaignUserId campaignUserId = new CampaignUserId();
                campaignUserId.setCampaign(examSession.getCampaign().getId());
                campaignUserId.setUser(examSession.getUser().getId());
                Optional<CampaignUser> optionalCampaignUser = campaignUserRepository.findById(campaignUserId);
                if(optionalCampaignUser.isPresent()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("score", examSession.getExamScore());
                    response.put("isArchived", optionalCampaignUser.get().isArchived());
                    return ResponseEntity.ok(response);
                }
            } else {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Exam not found."));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.internalServerError().body(Collections.singletonMap("error", SystemConstants.SOMETHING_WENT_WRONG));
    }

    @Override
    public ResponseEntity<Map<String, Object>> canUserTakeExam(Integer campaignId, Integer userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        Optional<Campaign> optionalCampaign = campaignRepository.findById(campaignId);
        if (optionalUser.isEmpty() || optionalCampaign.isEmpty())
            return ResponseEntity.ok(Collections.singletonMap("isUserCanTakeExam", false));
        User user = optionalUser.get();
        Campaign campaign = optionalCampaign.get();
        CampaignUserId campaignUserId = new CampaignUserId();
        campaignUserId.setCampaign(campaign.getId());
        campaignUserId.setUser(user.getId());

        Optional<CampaignUser> optionalCampaignUser = campaignUserRepository.findById(campaignUserId);
        if (!optionalCampaignUser.isPresent() || optionalCampaignUser.get().isArchived() || !optionalCampaignUser.get().isCompleted())
            return ResponseEntity.ok(Collections.singletonMap("isUserCanTakeExam", false));

        // We are certain that the user has not archived the final exam
        Optional<FinalExam> existingSession = examSessionRepository.findSession(user, campaign, false);
        if (existingSession.isPresent()) {
            FinalExam quizSession = existingSession.get();
            LocalDateTime now = LocalDateTime.now();
            Duration durationSinceStart = Duration.between(quizSession.getStartTime(), now);
            if (durationSinceStart.toMinutes() >= campaign.getTestOpenDuration()) {
                markQuizSessionAsCompleted(quizSession);
                return ResponseEntity.ok(Collections.singletonMap("isUserCanTakeExam", false)); //Quiz session has expired
            }
        } else {
            Optional<FinalExam> lastExamSession = examSessionRepository.findLastExamSessionByUserAndCampaign(user, campaign);
            if (lastExamSession.isPresent()) {
                FinalExam lastExam = lastExamSession.get();
                LocalDateTime startTime = lastExam.getStartTime();
                LocalDateTime now = LocalDateTime.now();
                Duration timeSinceStart = Duration.between(startTime, now);
                int retryTestDurationHours = campaign.getRetryTestDuration();
                Duration maxAllowedDuration = Duration.ofHours(retryTestDurationHours);
                if (timeSinceStart.compareTo(maxAllowedDuration) < 0) {
                    return ResponseEntity.ok(Collections.singletonMap("isUserCanTakeExam", false)); //Retry test is not allowed yet
                }
            }
        }

        boolean isContinue ;
        Optional<FinalExam> lastFinalExamSession = examSessionRepository.findLastExamSessionByUserAndCampaign(user, campaign);
        if (lastFinalExamSession.isPresent()) {
            isContinue = !lastFinalExamSession.get().isCompleted(); // continue,retry
        } else {
            isContinue = false; // start
        }
        Map<String, Object> response = new HashMap<>();
        response.put("isUserCanTakeExam", true);
        response.put("isContinueExam", isContinue);
        response.put("totalQuestions", campaign.getExamNumberQuestions());
        if(isContinue) response.put("nbQuestionsAttempted", lastFinalExamSession.get().getExamQuestions().size());
        return ResponseEntity.ok(response);
    }

    private void markQuizSessionAsCompleted(FinalExam quizSession) {
        quizSession.setCompleted(true);
        //quizSession.getUsedQuestions().clear();
        LocalDateTime now = LocalDateTime.now();
        quizSession.setEndTime(now);
        handleCampaignArchiveStatus(quizSession);
        examSessionRepository.save(quizSession);
    }

    private void handleCampaignArchiveStatus(FinalExam quizSession) {
        Optional<FinalExam> lastExamSession = examSessionRepository.findLastExamSessionByUserAndCampaign(quizSession.getUser(), quizSession.getCampaign());
        CampaignUserId campaignUserId = new CampaignUserId();
        campaignUserId.setCampaign(quizSession.getCampaign().getId());
        campaignUserId.setUser(quizSession.getUser().getId());
        Optional<CampaignUser> optionalCampaignUser = campaignUserRepository.findById(campaignUserId);
        if(optionalCampaignUser.isPresent()) {
            CampaignUser campaignUser = optionalCampaignUser.get();
            if(lastExamSession.get().getExamScore() >= quizSession.getCampaign().getRequiredExamScore())
                campaignUser.setArchived(true);
            else campaignUser.setArchived(false);
            campaignUserRepository.save(campaignUser);
        }
    }

    public List<QuestionWrapper> getQuestions(FinalExam finalExam) {
        try {
            List<QuestionWrapper> allQuestions = new ArrayList<>();

            List<Question> allQuestionsList = questionRepository.findAll();

            Set<ExamQuestion> usedQuestions = finalExam.getExamQuestions(); // Get questions already used in this exam
            int usedQuestionCount = usedQuestions.size(); // Count of used questions

            Campaign campaign = finalExam.getCampaign();
            int totalQuestionCount = campaign.getExamNumberQuestions(); // Total number of questions defined in the campaign

            Integer count = totalQuestionCount - usedQuestionCount; // Remaining questions to fetch

            // Filter out the used questions from allQuestionsList
            List<Question> questions = allQuestionsList.stream()
                    .filter(question -> !usedQuestions.contains(question))
                    .collect(Collectors.toList());

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
                return new ArrayList<>();
            }

            for (Question question : questions) {
                QuestionFactory.QuestionType type = getQuestionType(question);
                allQuestions.add(new QuestionWrapper(question, type));
            }

            return allQuestions;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
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

    private Boolean isResponseCorrect(Map<String, Object> requestMap) throws JSONException {
        if (requestMap.containsKey("questionId")) {
            Optional<Question> question = questionRepository.findById((Integer) requestMap.get("questionId"));

            if(question.isPresent()) {

                if (question.get() instanceof TrueFalseQuestion) {
                    if(requestMap.containsKey("isCorrect")) {
                        TrueFalseQuestion trueFalseQuestion = (TrueFalseQuestion) question.get();
                        boolean correctAnswer = trueFalseQuestion.isCorrect();
                        Boolean isCorrectValue = (Boolean) requestMap.get("isCorrect");
                        boolean userAnswer = isCorrectValue.booleanValue(); // Convert Boolean to boolean primitive
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
                                if (blockText.trim().isEmpty()) return false;
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
}