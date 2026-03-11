package com.wsy.mistake_notebook.service;

import com.wsy.mistake_notebook.dto.ReviewSubmitDTO;
import com.wsy.mistake_notebook.vo.ReviewQuestionVO;
import com.wsy.mistake_notebook.vo.ReviewTodaySummaryVO;

import java.util.List;
import java.util.Map;

public interface ReviewService {

    ReviewTodaySummaryVO getTodaySummary(Long userId);

    List<ReviewQuestionVO> listDueQuestions(Long userId);

    List<ReviewQuestionVO> listDueNowQuestions(Long userId);

    List<ReviewQuestionVO> listTodayPlanQuestions(Long userId);

    ReviewQuestionVO getNextQuestion(Long userId);

    ReviewQuestionVO getQuestion(Long userId, Long questionId);

    Map<String, Object> submitReview(Long userId, Long questionId, ReviewSubmitDTO dto);
}
