package com.wsy.mistake_notebook.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WrongQuestionManageVO {

    private Long id;
    private String subject;
    private String knowledgePoint;
    private String sourceType;
    private String errorReason;
    private Integer difficulty;
    private String status;
    private Boolean favorite;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String content;
    private String answer;
    private String analysis;

    private String imageUrl;
    private String ocrStatus;
    private String ocrText;

    private List<QuestionTagVO> tags;
}

