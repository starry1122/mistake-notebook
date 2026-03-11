package com.wsy.mistake_notebook.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
public class WrongQuestionUpdateDTO {

    private String subject;
    private String knowledgePoint;
    private String errorReason;

    @Min(1)
    @Max(5)
    private Integer difficulty;

    private String status;
    private Boolean favorite;

    private String content;
    private String answer;
    private String analysis;

    private List<Long> tagIds;
    private List<String> tagNames;
}

