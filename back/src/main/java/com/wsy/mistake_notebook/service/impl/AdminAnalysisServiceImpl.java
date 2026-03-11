package com.wsy.mistake_notebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsy.mistake_notebook.entity.User;
import com.wsy.mistake_notebook.entity.WrongQuestion;
import com.wsy.mistake_notebook.mapper.UserMapper;
import com.wsy.mistake_notebook.mapper.WrongQuestionMapper;
import com.wsy.mistake_notebook.service.AdminAnalysisService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminAnalysisServiceImpl implements AdminAnalysisService {

    private static final String ROLE_STUDENT = "student";
    private static final String STATUS_MASTERED = "\u5df2\u638c\u63e1";

    private final UserMapper userMapper;
    private final WrongQuestionMapper wrongQuestionMapper;

    public AdminAnalysisServiceImpl(UserMapper userMapper, WrongQuestionMapper wrongQuestionMapper) {
        this.userMapper = userMapper;
        this.wrongQuestionMapper = wrongQuestionMapper;
    }

    @Override
    public List<Map<String, Object>> listStudentOverviewList() {
        List<User> students = listStudents();
        if (students.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toList());
        List<WrongQuestion> allQuestions = listQuestionsByStudentIds(studentIds);

        Map<Long, List<WrongQuestion>> questionsByStudentId = allQuestions.stream()
                .collect(Collectors.groupingBy(WrongQuestion::getUserId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (User student : students) {
            List<WrongQuestion> studentQuestions = questionsByStudentId.getOrDefault(student.getId(), new ArrayList<>());
            result.add(buildStudentOverviewSummary(student, studentQuestions));
        }

        result.sort((a, b) -> {
            int compareWrongCount = Long.compare(toLong(b.get("wrongCount")), toLong(a.get("wrongCount")));
            if (compareWrongCount != 0) {
                return compareWrongCount;
            }
            return String.valueOf(a.get("username")).compareTo(String.valueOf(b.get("username")));
        });
        return result;
    }

    @Override
    public Map<String, Object> getStudentOverview(Long studentId) {
        User student = getStudentById(studentId);
        List<WrongQuestion> questions = listQuestionsByStudentId(studentId);

        Map<String, Object> baseSummary = buildStudentOverviewSummary(student, questions);
        List<Map<String, Object>> subjectStats = buildDistribution(questions, WrongQuestion::getSubject, "\u672a\u5206\u7c7b");
        List<Map<String, Object>> errorReasonStats = buildDistribution(questions, WrongQuestion::getErrorReason, "\u672a\u6807\u6ce8\u539f\u56e0");
        List<Map<String, Object>> knowledgePointStats = buildDistribution(questions, WrongQuestion::getKnowledgePoint, "\u672a\u6807\u6ce8\u77e5\u8bc6\u70b9");

        Map<String, Object> result = new HashMap<>(baseSummary);
        result.put("subjectStats", subjectStats);
        result.put("errorReasonStats", errorReasonStats);
        result.put("knowledgePointStats", limitTopN(knowledgePointStats, 8));
        return result;
    }

    @Override
    public Map<String, Object> getSystemStats(Integer topN) {
        int knowledgeTopN = normalizeTopN(topN);

        List<User> students = listStudents();
        List<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toList());
        List<WrongQuestion> allQuestions = listQuestionsByStudentIds(studentIds);

        long totalWrongCount = allQuestions.size();
        long masteredCount = allQuestions.stream().filter(this::isMastered).count();
        int masteryRate = toPercent(masteredCount, totalWrongCount);

        List<Map<String, Object>> subjectStats = buildDistribution(allQuestions, WrongQuestion::getSubject, "\u672a\u5206\u7c7b");
        List<Map<String, Object>> errorReasonStats = buildDistribution(allQuestions, WrongQuestion::getErrorReason, "\u672a\u6807\u6ce8\u539f\u56e0");
        List<Map<String, Object>> knowledgePointStats = buildDistribution(allQuestions, WrongQuestion::getKnowledgePoint, "\u672a\u6807\u6ce8\u77e5\u8bc6\u70b9");

        Map<String, Object> result = new HashMap<>();
        result.put("studentCount", students.size());
        result.put("totalWrongCount", totalWrongCount);
        result.put("masteryRate", masteryRate);
        result.put("subjectStats", subjectStats);
        result.put("errorReasonStats", errorReasonStats);
        result.put("knowledgePointConcentration", limitTopN(knowledgePointStats, knowledgeTopN));
        return result;
    }

    private User getStudentById(Long studentId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId, studentId)
                .eq(User::getRole, ROLE_STUDENT)
                .last("limit 1");

        User student = userMapper.selectOne(wrapper);
        if (student == null) {
            throw new RuntimeException("\u5b66\u751f\u4e0d\u5b58\u5728");
        }
        return student;
    }

    private List<User> listStudents() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, ROLE_STUDENT)
                .orderByAsc(User::getId);
        return userMapper.selectList(wrapper);
    }

    private List<WrongQuestion> listQuestionsByStudentId(Long studentId) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getUserId, studentId)
                .orderByDesc(WrongQuestion::getCreateTime)
                .orderByDesc(WrongQuestion::getId);
        return wrongQuestionMapper.selectList(wrapper);
    }

    private List<WrongQuestion> listQuestionsByStudentIds(List<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WrongQuestion::getUserId, studentIds)
                .orderByDesc(WrongQuestion::getCreateTime)
                .orderByDesc(WrongQuestion::getId);
        return wrongQuestionMapper.selectList(wrapper);
    }

    private Map<String, Object> buildStudentOverviewSummary(User student, List<WrongQuestion> questions) {
        long total = questions.size();
        long mastered = questions.stream().filter(this::isMastered).count();
        long unmastered = Math.max(total - mastered, 0L);

        Map<String, Long> knowledgeCountMap = new HashMap<>();
        for (WrongQuestion question : questions) {
            String key = defaultLabel(question.getKnowledgePoint(), "\u672a\u6807\u6ce8\u77e5\u8bc6\u70b9");
            knowledgeCountMap.put(key, knowledgeCountMap.getOrDefault(key, 0L) + 1L);
        }

        String topKnowledgePoint = "\u6682\u65e0";
        long topKnowledgePointCount = 0L;
        for (Map.Entry<String, Long> entry : knowledgeCountMap.entrySet()) {
            if (entry.getValue() > topKnowledgePointCount) {
                topKnowledgePoint = entry.getKey();
                topKnowledgePointCount = entry.getValue();
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("studentId", student.getId());
        result.put("username", student.getUsername());
        result.put("createTime", student.getCreateTime());
        result.put("wrongCount", total);
        result.put("masteredCount", mastered);
        result.put("unmasteredCount", unmastered);
        result.put("masteryRate", toPercent(mastered, total));
        result.put("highFrequencyKnowledgePoint", topKnowledgePoint);
        result.put("highFrequencyKnowledgePointCount", topKnowledgePointCount);
        return result;
    }

    private List<Map<String, Object>> buildDistribution(
            List<WrongQuestion> questions,
            Function<WrongQuestion, String> classifier,
            String fallbackLabel
    ) {
        if (questions == null || questions.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Long> countMap = new HashMap<>();
        for (WrongQuestion question : questions) {
            String key = defaultLabel(classifier.apply(question), fallbackLabel);
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

    private List<Map<String, Object>> limitTopN(List<Map<String, Object>> list, int topN) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        int limit = Math.min(topN, list.size());
        return new ArrayList<>(list.subList(0, limit));
    }

    private boolean isMastered(WrongQuestion question) {
        if (question == null) {
            return false;
        }
        String status = defaultLabel(question.getStatus(), "");
        return STATUS_MASTERED.equals(status)
                || "MASTERED".equalsIgnoreCase(status);
    }

    private int toPercent(long value, long total) {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.round(value * 100.0 / total);
    }

    private String defaultLabel(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? fallback : normalized;
    }

    private int normalizeTopN(Integer topN) {
        if (topN == null || topN < 1) {
            return 10;
        }
        return Math.min(topN, 30);
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
}
