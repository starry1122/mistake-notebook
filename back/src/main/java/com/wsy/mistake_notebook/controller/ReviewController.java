package com.wsy.mistake_notebook.controller;

import com.wsy.mistake_notebook.dto.ReviewSubmitDTO;
import com.wsy.mistake_notebook.service.ReviewService;
import com.wsy.mistake_notebook.utils.AuthUtil;
import com.wsy.mistake_notebook.vo.Result;
import com.wsy.mistake_notebook.vo.ReviewQuestionVO;
import com.wsy.mistake_notebook.vo.ReviewTodaySummaryVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/summary")
    public Result<ReviewTodaySummaryVO> getSummary(HttpServletRequest request) {
        Long userId = AuthUtil.requireStudentUserId(request);
        ReviewTodaySummaryVO summary = reviewService.getTodaySummary(userId);
        if (summary != null) {
            normalizeQuestionListUrls(request, summary.getDueNowPreview());
            normalizeQuestionListUrls(request, summary.getTodayPlanPreview());
            normalizeQuestionListUrls(request, summary.getTodayPreview());
        }
        return Result.success(summary);
    }

    @GetMapping("/due")
    public Result<List<ReviewQuestionVO>> listDueQuestions(HttpServletRequest request) {
        Long userId = AuthUtil.requireStudentUserId(request);
        List<ReviewQuestionVO> list = reviewService.listDueNowQuestions(userId);
        normalizeQuestionListUrls(request, list);
        return Result.success(list);
    }

    @GetMapping("/due-now")
    public Result<List<ReviewQuestionVO>> listDueNowQuestions(HttpServletRequest request) {
        Long userId = AuthUtil.requireStudentUserId(request);
        List<ReviewQuestionVO> list = reviewService.listDueNowQuestions(userId);
        normalizeQuestionListUrls(request, list);
        return Result.success(list);
    }

    @GetMapping("/today-plan")
    public Result<List<ReviewQuestionVO>> listTodayPlanQuestions(HttpServletRequest request) {
        Long userId = AuthUtil.requireStudentUserId(request);
        List<ReviewQuestionVO> list = reviewService.listTodayPlanQuestions(userId);
        normalizeQuestionListUrls(request, list);
        return Result.success(list);
    }

    @GetMapping("/next")
    public Result<ReviewQuestionVO> getNextQuestion(HttpServletRequest request) {
        Long userId = AuthUtil.requireStudentUserId(request);
        ReviewQuestionVO vo = reviewService.getNextQuestion(userId);
        if (vo != null) {
            vo.setImageUrl(toAbsoluteUrl(request, vo.getImageUrl()));
        }
        return Result.success(vo);
    }

    @GetMapping("/question/{questionId:[0-9]+}")
    public Result<ReviewQuestionVO> getQuestion(
            @PathVariable Long questionId,
            HttpServletRequest request
    ) {
        Long userId = AuthUtil.requireStudentUserId(request);
        ReviewQuestionVO vo = reviewService.getQuestion(userId, questionId);
        vo.setImageUrl(toAbsoluteUrl(request, vo.getImageUrl()));
        return Result.success(vo);
    }

    @PostMapping("/question/{questionId:[0-9]+}/submit")
    public Result<Map<String, Object>> submitReview(
            @PathVariable Long questionId,
            @Valid @RequestBody ReviewSubmitDTO dto,
            HttpServletRequest request
    ) {
        Long userId = AuthUtil.requireStudentUserId(request);
        Map<String, Object> result = reviewService.submitReview(userId, questionId, dto);
        Object questionObj = result.get("question");
        if (questionObj instanceof ReviewQuestionVO) {
            ReviewQuestionVO vo = (ReviewQuestionVO) questionObj;
            vo.setImageUrl(toAbsoluteUrl(request, vo.getImageUrl()));
        }
        return Result.success(result);
    }

    private void normalizeQuestionListUrls(HttpServletRequest request, List<ReviewQuestionVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (ReviewQuestionVO item : list) {
            item.setImageUrl(toAbsoluteUrl(request, item.getImageUrl()));
        }
    }

    private String toAbsoluteUrl(HttpServletRequest request, String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return rawUrl;
        }
        if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            return rawUrl;
        }
        if (rawUrl.startsWith("/")) {
            return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + rawUrl;
        }
        return rawUrl;
    }
}
