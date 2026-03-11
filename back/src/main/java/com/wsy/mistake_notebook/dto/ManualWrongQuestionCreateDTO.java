package com.wsy.mistake_notebook.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ManualWrongQuestionCreateDTO {

    private String subject;
    private String knowledgePoint;

    @Size(max = 255)
    private String errorReason;

    @Min(1) @Max(5)
    private Integer difficulty;

    private Boolean favorite;

    private String content;
    private String answer;
    private String analysis;

    private List<String> tagNames;
    private List<Long> tagIds;

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getKnowledgePoint() { return knowledgePoint; }
    public void setKnowledgePoint(String knowledgePoint) { this.knowledgePoint = knowledgePoint; }

    public String getErrorReason() { return errorReason; }
    public void setErrorReason(String errorReason) { this.errorReason = errorReason; }

    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }

    public Boolean getFavorite() { return favorite; }
    public void setFavorite(Boolean favorite) { this.favorite = favorite; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }

    public List<String> getTagNames() { return tagNames; }
    public void setTagNames(List<String> tagNames) { this.tagNames = tagNames; }

    public List<Long> getTagIds() { return tagIds; }
    public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }
}
