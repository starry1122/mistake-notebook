package com.wsy.mistake_notebook.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewTodaySummaryVO {

    private Long dueNowCount;
    private Long dueTodayCount;
    private Long overdueCount;
    private Long dueLaterTodayCount;
    private LocalDateTime nextReviewTime;
    private List<ReviewQuestionVO> dueNowPreview;
    private List<ReviewQuestionVO> todayPlanPreview;
    private List<ReviewQuestionVO> todayPreview;
}
