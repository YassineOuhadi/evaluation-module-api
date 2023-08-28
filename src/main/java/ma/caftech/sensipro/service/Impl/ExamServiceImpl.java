package ma.caftech.sensipro.service.Impl;

import lombok.extern.slf4j.Slf4j;
import ma.caftech.sensipro.domain.*;
import ma.caftech.sensipro.domain.id.CampaignUserId;
import ma.caftech.sensipro.dto.ChoiceQuestionDTO;
import ma.caftech.sensipro.dto.FillBlanksQuestionDTO;
import ma.caftech.sensipro.dto.QuestionDTO;
import ma.caftech.sensipro.dto.TrueFalseQuestionDTO;
import ma.caftech.sensipro.repository.*;
import ma.caftech.sensipro.service.service.ExamService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ma.caftech.sensipro.constants.SystemConstants;

import java.time.LocalDateTime;
import java.util.*;

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
    QuestionRepository questionRepository;

    @Override
    public ResponseEntity<Map<String, Object>> beginExam(Map<String, Object> requestMap) {
        Integer userId = (Integer) requestMap.get("userId");
        Integer campaignId = (Integer) requestMap.get("campaignId");

        Optional<AsUser> optionalUser = userRepository.findById(userId);
        Optional<Campaign> optionalCampaign = campaignRepository.findById(campaignId);

        if (optionalUser.isEmpty() || optionalCampaign.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "User or campaign not found."));
        }

        AsUser user = optionalUser.get();
        Campaign campaign = optionalCampaign.get();

        CampaignUserId campaignUserId = new CampaignUserId();
        campaignUserId.setCampaign(campaign.getId());
        campaignUserId.setUser(user.getId());

        Optional<CampaignUser> optionalCampaignUser = campaignUserRepository.findById(campaignUserId);
        if (!optionalCampaignUser.isPresent()) return ResponseEntity.badRequest().body(Collections.singletonMap("error", "User not active in this campaign."));

        CampaignUser campaignUser = optionalCampaignUser.get();
        if(campaignUser.isArchived()) return ResponseEntity.badRequest().body(Collections.singletonMap("error", "User has already archived this campaign."));
        if(!campaignUser.isCompleted()) return ResponseEntity.badRequest().body(Collections.singletonMap("error", "User has not completed the campaign."));

        //Optional<FinalExam> existingSession = examSessionRepository.findSession(user, campaign, false);

        //test nb attempt

        FinalExam quizSession = new FinalExam();
        quizSession.setUser(user);
        quizSession.setCampaign(campaign);
        quizSession.setStartTime(LocalDateTime.now());
        examSessionRepository.save(quizSession);

        List<QuestionDTO> questions = getQuestions(quizSession); // Modified to return QuestionDTO objects
        if (questions.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "No questions available."));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", quizSession.getId());
        //response.put("examSessionEndDate", endDate);
        response.put("questions", questions);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Map<String, Object>> canUserTakeExam(Integer campaignId, Integer userId) {
        Optional<AsUser> optionalUser = userRepository.findById(userId);
        Optional<Campaign> optionalCampaign = campaignRepository.findById(campaignId);
        if (optionalUser.isEmpty() || optionalCampaign.isEmpty())
            return ResponseEntity.ok(Collections.singletonMap("isUserCanTakeExam", false));
        AsUser user = optionalUser.get();
        Campaign campaign = optionalCampaign.get();
        CampaignUserId campaignUserId = new CampaignUserId();
        campaignUserId.setCampaign(campaign.getId());
        campaignUserId.setUser(user.getId());

        Optional<CampaignUser> optionalCampaignUser = campaignUserRepository.findById(campaignUserId);
        if (!optionalCampaignUser.isPresent() || optionalCampaignUser.get().isArchived() || !optionalCampaignUser.get().isCompleted())
            return ResponseEntity.ok(Collections.singletonMap("isUserCanTakeExam", false));

        Map<String, Object> response = new HashMap<>();
        response.put("isUserCanTakeExam", true);
        //response.put("isContinueExam", isContinue);
        response.put("totalQuestions", campaign.getNumberOfQuestionsInExam());
        //if(isContinue) response.put("nbQuestionsAttempted", lastFinalExamSession.get().getExamQuestions().size());
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
                    finalExam.setEndTime(LocalDateTime.now()); // last activity
                    boolean isCorrect = isResponseCorrect(requestMap);

                    double examScorePercentage = finalExam.getExamScore();
                    int totalCorrectAnswers = (int) (examScorePercentage * finalExam.getCampaign().getNumberOfQuestionsInExam() / 100);
                    if (isCorrect) totalCorrectAnswers++;
                    double scorePercentage = (double) totalCorrectAnswers / finalExam.getCampaign().getNumberOfQuestionsInExam() * 100;
                    finalExam.setExamScore(scorePercentage);

                    examSessionRepository.save(finalExam);
                    return new ResponseEntity<>(isCorrect, HttpStatus.OK);
                }
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<QuestionDTO> getQuestions(FinalExam finalExam) {
        try {
            List<QuestionDTO> allQuestions = new ArrayList<>();

            List<Question> allQuestionsList = questionRepository.findAll();

            Campaign campaign = finalExam.getCampaign();
            Integer totalQuestionCount = campaign.getNumberOfQuestionsInExam(); // Total number of questions defined in the campaign

            Random random = new Random();
            for (int i = allQuestionsList.size() - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                Question temp = allQuestionsList.get(i);
                allQuestionsList.set(i, allQuestionsList.get(j));
                allQuestionsList.set(j, temp);
            }

            if (totalQuestionCount != null && totalQuestionCount > 0 && totalQuestionCount <= allQuestionsList.size()) {
                allQuestionsList = allQuestionsList.subList(0, totalQuestionCount);
            } else if (totalQuestionCount != null && totalQuestionCount > allQuestionsList.size()) {
                return new ArrayList<>();
            }

            for (Question question : allQuestionsList) {
                QuestionDTO questionDTO = mapQuestionToDTO(question);
                allQuestions.add(questionDTO);
            }

            return allQuestions;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private QuestionDTO mapQuestionToDTO(Question question) {
        if (question instanceof ChoiceQuestion) {
            return ChoiceQuestionDTO.fromChoiceQuestionDTO((ChoiceQuestion) question);
        } else if (question instanceof TrueFalseQuestion) {
            return TrueFalseQuestionDTO.fromTrueFalseQuestion((TrueFalseQuestion) question);
        } else if (question instanceof FillBlanksQuestion) {
            return FillBlanksQuestionDTO.fromFillBlanksQuestion((FillBlanksQuestion) question);
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

    /*
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

    private void markQuizSessionAsCompleted(FinalExam quizSession) {
        quizSession.setCompleted(true);
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
            if(lastExamSession.get().getExamScore() >= quizSession.getCampaign().getArchivingScore())
                campaignUser.setArchived(true);
            else campaignUser.setArchived(false);
            campaignUserRepository.save(campaignUser);
        }
    }
     */
}