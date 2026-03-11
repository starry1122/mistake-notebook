package com.wsy.mistake_notebook.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewQuestionVO {

    private Long questionId;
    private String subject;
    private String knowledgePoint;
    private String errorReason;
    private Integer difficulty;
    private String status;

    private String content;
    private String answer;
    private String analysis;
    private String imageUrl;

    private Integer reviewCount;
    private Integer masteryLevel;
    private LocalDateTime lastReviewTime;
    private LocalDateTime nextReviewTime;

    private List<QuestionTagVO> tags;
}

