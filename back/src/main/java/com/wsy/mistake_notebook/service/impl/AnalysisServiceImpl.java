package com.wsy.mistake_notebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsy.mistake_notebook.entity.AnalysisReport;
import com.wsy.mistake_notebook.entity.WrongQuestion;
import com.wsy.mistake_notebook.mapper.AnalysisReportMapper;
import com.wsy.mistake_notebook.mapper.WrongQuestionMapper;
import com.wsy.mistake_notebook.service.AnalysisService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    private static final String STATUS_MASTERED = "\u5df2\u638c\u63e1";

    private final WrongQuestionMapper wrongQuestionMapper;
    private final AnalysisReportMapper analysisReportMapper;

    public AnalysisServiceImpl(WrongQuestionMapper wrongQuestionMapper, AnalysisReportMapper analysisReportMapper) {
        this.wrongQuestionMapper = wrongQuestionMapper;
        this.analysisReportMapper = analysisReportMapper;
    }

    @Override
    public Map<String, Object> getStudentOverview(Long userId) {
        List<WrongQuestion> allQuestions = listQuestionsByUser(userId);
        long total = allQuestions.size();
        long mastered = allQuestions.stream().filter(this::isMastered).count();
        long todayReview = Math.max(total - mastered, 0);
        int rate = total == 0 ? 0 : (int) Math.round(mastered * 100.0 / total);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("todayReview", todayReview);
        result.put("mastered", mastered);
        result.put("rate", rate);
        return result;
    }

    @Override
    public Map<String, Object> getStudentAnalysisDashboard(Long userId, Integer topN, String subject) {
        int weakTopN = normalizeTopN(topN);
        String normalizedSubject = normalizeSubject(subject);

        List<WrongQuestion> allQuestions = listQuestionsByUser(userId);
        List<WrongQuestion> scopedQuestions = filterBySubject(allQuestions, normalizedSubject);

        List<Map<String, Object>> subjectStats = buildSubjectStats(allQuestions);
        List<Map<String, Object>> errorReasonStats = buildErrorReasonStats(scopedQuestions);
        List<Map<String, Object>> weakKnowledgePoints = buildWeakKnowledgePoints(scopedQuestions, weakTopN);

        String reportContent = generateReportText(scopedQuestions, subjectStats, errorReasonStats, weakKnowledgePoints, normalizedSubject);
        AnalysisReport report = saveReport(userId, normalizedSubject, reportContent);

        Map<String, Object> reportMap = new HashMap<>();
        reportMap.put("id", report.getId());
        reportMap.put("subject", report.getSubject());
        reportMap.put("content", report.getContent());
        reportMap.put("createTime", report.getCreateTime());

        Map<String, Object> result = new HashMap<>();
        result.put("subject", normalizedSubject);
        result.put("totalWrongCount", scopedQuestions.size());
        result.put("subjectStats", subjectStats);
        result.put("errorReasonStats", errorReasonStats);
        result.put("weakKnowledgePoints", weakKnowledgePoints);
        result.put("report", reportMap);
        return result;
    }

    private List<WrongQuestion> listQuestionsByUser(Long userId) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getUserId, userId)
                .orderByDesc(WrongQuestion::getCreateTime)
                .orderByDesc(WrongQuestion::getId);
        return wrongQuestionMapper.selectList(wrapper);
    }

    private List<WrongQuestion> filterBySubject(List<WrongQuestion> questions, String subject) {
        if (!StringUtils.hasText(subject)) {
            return questions;
        }

        return questions.stream()
                .filter(item -> subject.equals(trimToEmpty(item.getSubject())))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildSubjectStats(List<WrongQuestion> questions) {
        if (questions.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Long> countMap = new HashMap<>();
        for (WrongQuestion question : questions) {
            String key = defaultLabel(question.getSubject(), "\u672a\u5206\u7c7b");
            countMap.put(key, countMap.getOrDefault(key, 0L) + 1L);
        }

        long total = questions.size();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : countMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("count", entry.getValue());
            item.put("percent", toPercent(entry.getValue(), total));
            result.add(item);
        }

        result.sort(Comparator.comparingLong(item -> -toLong(item.get("count"))));
        return result;
    }

    private List<Map<String, Object>> buildErrorReasonStats(List<WrongQuestion> questions) {
        if (questions.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Long> countMap = new HashMap<>();
        for (WrongQuestion question : questions) {
            String key = defaultLabel(question.getErrorReason(), "\u672a\u6807\u6ce8\u539f\u56e0");
            countMap.put(key, countMap.getOrDefault(key, 0L) + 1L);
        }

        long total = questions.size();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : countMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("count", entry.getValue());
            item.put("percent", toPercent(entry.getValue(), total));
            result.add(item);
        }

        result.sort(Comparator.comparingLong(item -> -toLong(item.get("count"))));
        return result;
    }

    private List<Map<String, Object>> buildWeakKnowledgePoints(List<WrongQuestion> questions, int topN) {
        if (questions.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, WeakPointAgg> aggMap = new HashMap<>();
        for (WrongQuestion question : questions) {
            String key = defaultLabel(question.getKnowledgePoint(), "\u672a\u6807\u6ce8\u77e5\u8bc6\u70b9");
            WeakPointAgg agg = aggMap.computeIfAbsent(key, item -> new WeakPointAgg());
            agg.totalCount++;
            if (!isMastered(question)) {
                agg.unmasteredCount++;
            }

            if (question.getDifficulty() != null && question.getDifficulty() > 0) {
                agg.difficultySum += question.getDifficulty();
                agg.difficultySamples += 1;
            }
        }

        List<Map.Entry<String, WeakPointAgg>> sorted = new ArrayList<>(aggMap.entrySet());
        sorted.sort((a, b) -> {
            int compareUnmastered = Integer.compare(b.getValue().unmasteredCount, a.getValue().unmasteredCount);
            if (compareUnmastered != 0) {
                return compareUnmastered;
            }
            return Integer.compare(b.getValue().totalCount, a.getValue().totalCount);
        });

        List<Map<String, Object>> result = new ArrayList<>();
        int limit = Math.min(topN, sorted.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, WeakPointAgg> entry = sorted.get(i);
            WeakPointAgg agg = entry.getValue();

            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("totalCount", agg.totalCount);
            item.put("unmasteredCount", agg.unmasteredCount);
            item.put("masteryRate", toPercent(agg.totalCount - agg.unmasteredCount, agg.totalCount));
            item.put("avgDifficulty", agg.avgDifficulty());
            result.add(item);
        }
        return result;
    }

    private String generateReportText(
            List<WrongQuestion> scopedQuestions,
            List<Map<String, Object>> subjectStats,
            List<Map<String, Object>> errorReasonStats,
            List<Map<String, Object>> weakKnowledgePoints,
            String subject
    ) {
        int total = scopedQuestions.size();
        long mastered = scopedQuestions.stream().filter(this::isMastered).count();
        int masteryRate = total == 0 ? 0 : toPercent(mastered, total);

        String scopeLabel = StringUtils.hasText(subject)
                ? "\u672c\u6b21\u5206\u6790\u8303\u56f4\uff1a" + subject + "\u3002"
                : "\u672c\u6b21\u5206\u6790\u8303\u56f4\uff1a\u5168\u90e8\u79d1\u76ee\u3002";

        String topSubject = subjectStats.isEmpty() ? "\u6682\u65e0" : String.valueOf(subjectStats.get(0).get("name"));
        String topErrorReason = errorReasonStats.isEmpty() ? "\u6682\u65e0" : String.valueOf(errorReasonStats.get(0).get("name"));

        String weakPointLine;
        if (weakKnowledgePoints.isEmpty()) {
            weakPointLine = "\u6682\u672a\u8bc6\u522b\u5230\u660e\u663e\u8584\u5f31\u77e5\u8bc6\u70b9\u3002";
        } else {
            List<String> names = weakKnowledgePoints.stream()
                    .map(item -> String.valueOf(item.get("name")))
                    .collect(Collectors.toList());
            weakPointLine = "\u76ee\u524d\u8584\u5f31\u77e5\u8bc6\u70b9\u4e3b\u8981\u96c6\u4e2d\u5728\uff1a" + String.join("\u3001", names) + "\u3002";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("\u3010\u5b66\u60c5\u5206\u6790\u62a5\u544a\u3011").append('\n');
        builder.append(scopeLabel).append('\n');
        builder.append("\u9519\u9898\u603b\u91cf\uff1a").append(total).append("\uff0c\u638c\u63e1\u7387\uff1a").append(masteryRate).append("%\u3002").append('\n');
        builder.append("\u9519\u9898\u6700\u591a\u79d1\u76ee\uff1a").append(topSubject).append("\u3002").append('\n');
        builder.append("\u9ad8\u9891\u9519\u8bef\u539f\u56e0\uff1a").append(topErrorReason).append("\u3002").append('\n');
        builder.append(weakPointLine).append('\n');
        builder.append("\u5efa\u8bae\uff1a\u4f18\u5148\u5bf9\u8584\u5f31\u77e5\u8bc6\u70b9\u8fdb\u884c\u4e13\u9879\u590d\u4e60\uff0c\u5e76\u7ed3\u5408\u9519\u56e0\u5206\u5e03\u4f18\u5316\u5ba1\u9898\u4e0e\u7b54\u9898\u4e60\u60ef\u3002");
        return builder.toString();
    }

    private AnalysisReport saveReport(Long userId, String subject, String content) {
        AnalysisReport report = new AnalysisReport();
        report.setUserId(userId);
        report.setSubject(subject);
        report.setContent(content);
        analysisReportMapper.insert(report);
        return report;
    }

    private boolean isMastered(WrongQuestion question) {
        if (question == null) {
            return false;
        }
        String status = trimToEmpty(question.getStatus());
        return STATUS_MASTERED.equals(status)
                || "MASTERED".equalsIgnoreCase(status);
    }

    private int toPercent(long value, long total) {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.round(value * 100.0 / total);
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String normalizeSubject(String subject) {
        String value = trimToEmpty(subject);
        return value.isEmpty() ? null : value;
    }

    private int normalizeTopN(Integer topN) {
        if (topN == null || topN < 1) {
            return 5;
        }
        return Math.min(topN, 20);
    }

    private String defaultLabel(String value, String fallback) {
        String normalized = trimToEmpty(value);
        return normalized.isEmpty() ? fallback : normalized;
    }

    private String trimToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static class WeakPointAgg {
        private int totalCount;
        private int unmasteredCount;
        private int difficultySum;
        private int difficultySamples;

        private double avgDifficulty() {
            if (difficultySamples == 0) {
                return 0.0;
            }
            return Math.round((difficultySum * 1.0 / difficultySamples) * 100.0) / 100.0;
        }
    }
}

