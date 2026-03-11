package com.wsy.mistake_notebook.controller;

import com.wsy.mistake_notebook.service.AdminAnalysisService;
import com.wsy.mistake_notebook.utils.AuthUtil;
import com.wsy.mistake_notebook.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/analysis")
public class AdminAnalysisController {

    private final AdminAnalysisService adminAnalysisService;

    public AdminAnalysisController(AdminAnalysisService adminAnalysisService) {
        this.adminAnalysisService = adminAnalysisService;
    }

    @GetMapping("/students")
    public Result<List<Map<String, Object>>> listStudents(HttpServletRequest request) {
        AuthUtil.requireAdminUserId(request);
        return Result.success(adminAnalysisService.listStudentOverviewList());
    }

    @GetMapping("/students/{studentId}/overview")
    public Result<Map<String, Object>> getStudentOverview(
            @PathVariable Long studentId,
            HttpServletRequest request
    ) {
        AuthUtil.requireAdminUserId(request);
        return Result.success(adminAnalysisService.getStudentOverview(studentId));
    }

    @GetMapping("/system")
    public Result<Map<String, Object>> getSystemStats(
            @RequestParam(required = false) Integer topN,
            HttpServletRequest request
    ) {
        AuthUtil.requireAdminUserId(request);
        return Result.success(adminAnalysisService.getSystemStats(topN));
    }
}

