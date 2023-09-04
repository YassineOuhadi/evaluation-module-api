package ma.caftech.sensipro.service.Impl;

import lombok.extern.slf4j.Slf4j;
import ma.caftech.sensipro.domain.*;
import ma.caftech.sensipro.dto.*;
import ma.caftech.sensipro.mapper.QuestionMapper;
import ma.caftech.sensipro.repository.*;
import ma.caftech.sensipro.service.service.ExamService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    CampaignRepository campaignRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    CampaignProgressRepository campaignProgressRepository;

    @Autowired
    private QuestionMapper questionMapper;

    private static final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    public List<QuestionDTO> beginExam(Map<String, Object> requestMap) {
        log.info("Inside beginExam {}", requestMap);
        Integer campaignProgressId = (Integer) requestMap.get("campaignProgressId");
        Optional<CampaignProgress> optionalCampaignProgress = campaignProgressRepository.findById(campaignProgressId.longValue());

        if (optionalCampaignProgress.isEmpty())
            throw new IllegalArgumentException("Campaign progress not found.");

        CampaignProgress campaignProgress = optionalCampaignProgress.get();
        if (campaignProgress.getValue() < 70)
            throw new IllegalArgumentException("User has not completed the campaign.");

        campaignProgress.setNbreAttemptsExam(campaignProgress.getNbreAttemptsExam() + 1);
        campaignProgressRepository.save(campaignProgress);

        if(campaignProgress.getValue() != 100) {
            campaignProgress.setCorrectAnswers(0);
            campaignProgress.setScore(0.0);
            campaignProgressRepository.save(campaignProgress);
        }

        List<QuestionDTO> questions = getQuestions(campaignProgress.getLaunchCampaign().getCampaign());
        if (questions.isEmpty())
            throw new IllegalArgumentException("No questions available.");
        return questions;
    }

    @Override
    public Map<String, Object> canUserTakeExam(Long campaignProgressId) {
        log.info("Inside canUserTakeExam {}", campaignProgressId);
        Optional<CampaignProgress> optionalCampaignProgress = campaignProgressRepository.findById(campaignProgressId);
        if (optionalCampaignProgress.isEmpty())
            throw new IllegalArgumentException("Campaign progress not found.");

        CampaignProgress campaignProgress = optionalCampaignProgress.get();
        if (campaignProgress.getValue() < 70) {
            log.error("User has not completed the campaign.");
            return Collections.singletonMap("isUserCanTakeExam", false);
        }

        if(campaignProgress.getNbreAttemptsExam() >= campaignProgress.getLaunchCampaign().getCampaign().getMaxAttemptsAllowed()) {
            log.error("Retry test is not allowed yet.");
            return Collections.singletonMap("isUserCanTakeExam", false);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("isUserCanTakeExam", true);
        response.put("totalQuestions", campaignProgress.getLaunchCampaign().getCampaign().getNumberOfQuestionsInExam());
        return response;
    }

    @Override
    public Map<String, Object> validateResponse(Map<String, Object> requestMap) {
        log.info("Inside validate response {}", requestMap);
        try {
            Map<String, Object> response = new HashMap<>();
            if (requestMap.containsKey("campaignProgressId")) {
                Optional<CampaignProgress> optionalCampaignProgress = campaignProgressRepository.findById(((Integer) requestMap.get("campaignProgressId")).longValue());

                if (optionalCampaignProgress.isEmpty()) {
                    log.error("Campaign progress not found.");
                    throw new IllegalArgumentException("Campaign not found.");
                }

                if (optionalCampaignProgress.get().getValue() < 70) {
                    log.error("User can't take exam.");
                    throw new IllegalArgumentException("User can't take exam.");
                }

                CampaignProgress campaignProgress = optionalCampaignProgress.get();
                Double score = campaignProgress.getScore();
                Integer numberCorrectAnswers = campaignProgress.getCorrectAnswers();

                Boolean isCorrect = isResponseCorrect(requestMap);
                if(isCorrect == null) isCorrect = false;
                if(campaignProgress.getValue() < 100 && isCorrect) {
                    numberCorrectAnswers++;

                    score = (double )numberCorrectAnswers / campaignProgress.getLaunchCampaign().getCampaign().getNumberOfQuestionsInExam() * 100;
                    campaignProgress.setCorrectAnswers(numberCorrectAnswers);
                    campaignProgress.setScore(Double.valueOf(df.format(score)));

                    if(score >= campaignProgress.getLaunchCampaign().getCampaign().getArchivingScore()) {
                        campaignProgress.setValue(100);
                        campaignProgress.setEndDate(LocalDate.from(LocalDateTime.now()));
                    }
                    campaignProgressRepository.save(campaignProgress);
                }

                response.put("isCorrectAnswer", isCorrect);
                response.put("responseList", getQuestionAnswer(((Integer) requestMap.get("questionId")).longValue()));
                return response;
            }
            throw new IllegalArgumentException("Invalid requestMap from validateResponse.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> endExam(Long campaignProgressId) {
        log.info("Inside end exam {}", campaignProgressId);
        try {
            Optional<CampaignProgress> optionalCampaignProgress = campaignProgressRepository.findById(campaignProgressId);
            if (optionalCampaignProgress.isEmpty()) {
                throw new IllegalArgumentException("Campaign progress not found.");
            }
            CampaignProgress campaignProgress = optionalCampaignProgress.get();
            Double score = campaignProgress.getScore();
            Map<String, Object> response = new HashMap<>();
            response.put("score", score);
            boolean isArchived = score >= campaignProgress.getLaunchCampaign().getCampaign().getArchivingScore();
            response.put("isArchived", isArchived);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getQuestionAnswer(Long idQuestion) {
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

    public List<QuestionDTO> getQuestions(Campaign campaign) {
        try {
            List<QuestionDTO> allQuestions = new ArrayList<>();
            List<Question> allQuestionsList = questionRepository.findAll();
            Integer totalQuestionCount = campaign.getNumberOfQuestionsInExam();
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
                QuestionDTO questionDTO = questionMapper.toQuestionDTO(question);
                allQuestions.add(questionDTO);
            }
            return allQuestions;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private Boolean isResponseCorrect(Map<String, Object> requestMap) throws JSONException {
        if (requestMap.containsKey("questionId")) {
            Optional<Question> question = questionRepository.findById(((Integer) requestMap.get("questionId")).longValue());
            if(question.isPresent()) {
                if (question.get() instanceof TrueFalseQuestion) {
                    if(requestMap.containsKey("isCorrect")) {
                        TrueFalseQuestion trueFalseQuestion = (TrueFalseQuestion) question.get();


                        boolean correctAnswer = trueFalseQuestion.getIsCorrect();


                        Boolean isCorrectValue = (Boolean) requestMap.get("isCorrect");

                        if(isCorrectValue == null) return false;

                        boolean userAnswer = isCorrectValue != null && isCorrectValue.booleanValue();


                        return userAnswer == correctAnswer;
                    } else {
                        return false;
                    }
                }
                else if (question.get() instanceof ChoiceQuestion) {
                    if(requestMap.containsKey("options")) {
                        ChoiceQuestion choiceQuestion = (ChoiceQuestion) question.get();
                        List<Option> options = choiceQuestion.getOptions();
                        JSONArray optionsArray = new JSONArray((List<Object>) requestMap.get("options"));
                        for (int i = 0; i < optionsArray.length(); i++) {
                            JSONObject optionJson = optionsArray.getJSONObject(i);
                            Long optionId = optionJson.getLong("id");
                            boolean userAnswer = optionJson.getBoolean("isCorrect");
                            Option actualOption = options.stream().filter(option -> option.getId().equals(optionId)).findFirst().orElse(null);
                            if (actualOption != null) {
                                if (actualOption.getIsCorrect() != userAnswer) {
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
                        List<String> blocks = fillBlanksQuestion.extractBlocksFromText(true);
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

                                    if(!isExternalApiRunning()) {
                                        return validateResponseLocally(originalBlock, blockText);
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
                                            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
                                            if (response.getStatusCode().is2xxSuccessful()) {
                                                boolean isAnswerCorrect = (boolean) response.getBody().get("is_similar");
                                                if(!isAnswerCorrect) {
                                                    return false;
                                                }
                                            }
                                        } catch (Exception e) {
                                            //throw new RuntimeException(e);
                                            return validateResponseLocally(originalBlock, blockText);
                                        }
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