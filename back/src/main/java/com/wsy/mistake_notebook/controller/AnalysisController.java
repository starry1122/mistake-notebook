package com.wsy.mistake_notebook.controller;

import com.wsy.mistake_notebook.service.AnalysisService;
import com.wsy.mistake_notebook.utils.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/analysis/student")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/overview")
    public Map<String, Object> getOverview(HttpServletRequest request) {
        Long userId = AuthUtil.requireStudentUserId(request);
        return analysisService.getStudentOverview(userId);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard(
            @RequestParam(required = false) Integer topN,
            @RequestParam(required = false) String subject,
            HttpServletRequest request
    ) {
        Long userId = AuthUtil.requireStudentUserId(request);
        return analysisService.getStudentAnalysisDashboard(userId, topN, subject);
    }
}
