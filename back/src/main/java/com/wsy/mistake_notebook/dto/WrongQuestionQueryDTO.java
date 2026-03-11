package com.wsy.mistake_notebook.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class WrongQuestionQueryDTO {

    private String subject;
    private String knowledgePoint;
    private String status;
    private Integer difficulty;
    private Boolean favorite;
    private String keyword;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Integer pageNo = 1;
    private Integer pageSize = 10;
}

