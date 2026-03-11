package com.wsy.mistake_notebook.service;

import java.util.Map;

public interface AnalysisService {

    Map<String, Object> getStudentOverview(Long userId);

    Map<String, Object> getStudentAnalysisDashboard(Long userId, Integer topN, String subject);
}
