package com.example.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:8081", "http://127.0.0.1:8080"})
public class AiController {

    private final RestTemplate restTemplate;
    private static final String DEEPSEEK_URL = "https://api.deepseek.com/chat/completions";
    // 请替换为你实际的 API Token
    private static final String API_TOKEN = "sk-ba0b5e17decd42cbbcab21cb4fc697d9";

    @Autowired
    public AiController(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @GetMapping("/chat/{prompt}")
    public String chat(@PathVariable("prompt") String prompt) {
        // 构造请求对象，注意各个字段需要和 deepseek API 文档一致
        DeepseekRequest requestBody = new DeepseekRequest();
        requestBody.setModel("deepseek-chat");
        requestBody.setFrequencyPenalty(0);
        requestBody.setMaxTokens(2048);
        requestBody.setPresencePenalty(0);
        requestBody.setTemperature(1);
        requestBody.setTopP(1);
        requestBody.setLogprobs(false);
        requestBody.setToolChoice("none");
        requestBody.setStop(null);
        requestBody.setStream(false);
        requestBody.setStreamOptions(null);
        requestBody.setTools(null);
        requestBody.setTopLogprobs(null);

        // response_format 字段
        Map<String, String> responseFormat = new HashMap<>();
        responseFormat.put("type", "text");
        requestBody.setResponseFormat(responseFormat);

        // 设置对话消息：先设置 system，再设置 user
        List<Message> messages = new ArrayList<>();
        Message systemMessage = new Message();
        systemMessage.setRole("system");
        systemMessage.setContent("You are the AI assistant for Gym Panel, a small gym membership website. Your role is to help users navigate the platform and answer fitness-related questions in a friendly and helpful manner. Keep your responses concise and to the point.");
        messages.add(systemMessage);

        Message userMessage = new Message();
        userMessage.setRole("user");
        userMessage.setContent(prompt);
        messages.add(userMessage);
        requestBody.setMessages(messages);

        // 构造请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + API_TOKEN);

        HttpEntity<DeepseekRequest> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<DeepseekResponse> responseEntity = restTemplate.postForEntity(DEEPSEEK_URL, entity, DeepseekResponse.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
            DeepseekResponse deepseekResponse = responseEntity.getBody();
            if (deepseekResponse.getChoices() != null && !deepseekResponse.getChoices().isEmpty()) {
                return deepseekResponse.getChoices().get(0).getMessage().getContent();
            } else {
                return "未收到 deepseek 响应内容";
            }
        } else {
            return "请求错误，状态码：" + responseEntity.getStatusCode();
        }
    }
}

// ==================== 请求对象 ====================
class DeepseekRequest {

    @JsonProperty("messages")
    private List<Message> messages;

    @JsonProperty("model")
    private String model;

    @JsonProperty("frequency_penalty")
    private int frequencyPenalty;

    @JsonProperty("max_tokens")
    private int maxTokens;

    @JsonProperty("presence_penalty")
    private int presencePenalty;

    @JsonProperty("response_format")
    private Map<String, String> responseFormat;

    @JsonProperty("stop")
    private Object stop;

    @JsonProperty("stream")
    private boolean stream;

    @JsonProperty("stream_options")
    private Object streamOptions;

    @JsonProperty("temperature")
    private int temperature;

    @JsonProperty("top_p")
    private int topP;

    @JsonProperty("tools")
    private Object tools;

    @JsonProperty("tool_choice")
    private String toolChoice;

    @JsonProperty("logprobs")
    private boolean logprobs;

    @JsonProperty("top_logprobs")
    private Object topLogprobs;

    // Getters and Setters

    public List<Message> getMessages() {
        return messages;
    }
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public int getFrequencyPenalty() {
        return frequencyPenalty;
    }
    public void setFrequencyPenalty(int frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }
    public int getMaxTokens() {
        return maxTokens;
    }
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
    public int getPresencePenalty() {
        return presencePenalty;
    }
    public void setPresencePenalty(int presencePenalty) {
        this.presencePenalty = presencePenalty;
    }
    public Map<String, String> getResponseFormat() {
        return responseFormat;
    }
    public void setResponseFormat(Map<String, String> responseFormat) {
        this.responseFormat = responseFormat;
    }
    public Object getStop() {
        return stop;
    }
    public void setStop(Object stop) {
        this.stop = stop;
    }
    public boolean isStream() {
        return stream;
    }
    public void setStream(boolean stream) {
        this.stream = stream;
    }
    public Object getStreamOptions() {
        return streamOptions;
    }
    public void setStreamOptions(Object streamOptions) {
        this.streamOptions = streamOptions;
    }
    public int getTemperature() {
        return temperature;
    }
    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }
    public int getTopP() {
        return topP;
    }
    public void setTopP(int topP) {
        this.topP = topP;
    }
    public Object getTools() {
        return tools;
    }
    public void setTools(Object tools) {
        this.tools = tools;
    }
    public String getToolChoice() {
        return toolChoice;
    }
    public void setToolChoice(String toolChoice) {
        this.toolChoice = toolChoice;
    }
    public boolean isLogprobs() {
        return logprobs;
    }
    public void setLogprobs(boolean logprobs) {
        this.logprobs = logprobs;
    }
    public Object getTopLogprobs() {
        return topLogprobs;
    }
    public void setTopLogprobs(Object topLogprobs) {
        this.topLogprobs = topLogprobs;
    }
}

// ==================== 消息对象 ====================
class Message {

    @JsonProperty("role")
    private String role;

    @JsonProperty("content")
    private String content;

    // Getters and Setters
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}

// ==================== 响应对象 ====================
class DeepseekResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("object")
    private String object;

    @JsonProperty("created")
    private long created;

    @JsonProperty("model")
    private String model;

    @JsonProperty("choices")
    private List<Choice> choices;

    @JsonProperty("usage")
    private Usage usage;

    @JsonProperty("system_fingerprint")
    private String systemFingerprint;

    // Getters and Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getObject() {
        return object;
    }
    public void setObject(String object) {
        this.object = object;
    }
    public long getCreated() {
        return created;
    }
    public void setCreated(long created) {
        this.created = created;
    }
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public List<Choice> getChoices() {
        return choices;
    }
    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
    public Usage getUsage() {
        return usage;
    }
    public void setUsage(Usage usage) {
        this.usage = usage;
    }
    public String getSystemFingerprint() {
        return systemFingerprint;
    }
    public void setSystemFingerprint(String systemFingerprint) {
        this.systemFingerprint = systemFingerprint;
    }
}

// ==================== 选择项 ====================
class Choice {

    @JsonProperty("index")
    private int index;

    @JsonProperty("message")
    private Message message;

    @JsonProperty("logprobs")
    private Object logprobs;

    @JsonProperty("finish_reason")
    private String finishReason;

    // Getters and Setters
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
    public Message getMessage() {
        return message;
    }
    public void setMessage(Message message) {
        this.message = message;
    }
    public Object getLogprobs() {
        return logprobs;
    }
    public void setLogprobs(Object logprobs) {
        this.logprobs = logprobs;
    }
    public String getFinishReason() {
        return finishReason;
    }
    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }
}

// ==================== 使用情况 ====================
class Usage {

    @JsonProperty("prompt_tokens")
    private int promptTokens;

    @JsonProperty("completion_tokens")
    private int completionTokens;

    @JsonProperty("total_tokens")
    private int totalTokens;

    // Getters and Setters
    public int getPromptTokens() {
        return promptTokens;
    }
    public void setPromptTokens(int promptTokens) {
        this.promptTokens = promptTokens;
    }
    public int getCompletionTokens() {
        return completionTokens;
    }
    public void setCompletionTokens(int completionTokens) {
        this.completionTokens = completionTokens;
    }
    public int getTotalTokens() {
        return totalTokens;
    }
    public void setTotalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
    }
}
