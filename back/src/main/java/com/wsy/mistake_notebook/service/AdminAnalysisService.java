package com.wsy.mistake_notebook.service;

import java.util.List;
import java.util.Map;

public interface AdminAnalysisService {

    List<Map<String, Object>> listStudentOverviewList();

    Map<String, Object> getStudentOverview(Long studentId);

    Map<String, Object> getSystemStats(Integer topN);
}

