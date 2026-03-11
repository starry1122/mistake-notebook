package com.wsy.mistake_notebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsy.mistake_notebook.dto.ReviewSubmitDTO;
import com.wsy.mistake_notebook.entity.QuestionContent;
import com.wsy.mistake_notebook.entity.QuestionImage;
import com.wsy.mistake_notebook.entity.QuestionTag;
import com.wsy.mistake_notebook.entity.ReviewRecord;
import com.wsy.mistake_notebook.entity.Tag;
import com.wsy.mistake_notebook.entity.WrongQuestion;
import com.wsy.mistake_notebook.mapper.QuestionContentMapper;
import com.wsy.mistake_notebook.mapper.QuestionImageMapper;
import com.wsy.mistake_notebook.mapper.QuestionTagMapper;
import com.wsy.mistake_notebook.mapper.ReviewRecordMapper;
import com.wsy.mistake_notebook.mapper.TagMapper;
import com.wsy.mistake_notebook.mapper.WrongQuestionMapper;
import com.wsy.mistake_notebook.service.ReviewService;
import com.wsy.mistake_notebook.vo.QuestionTagVO;
import com.wsy.mistake_notebook.vo.ReviewQuestionVO;
import com.wsy.mistake_notebook.vo.ReviewTodaySummaryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final String STATUS_UNMASTERED = "\u672a\u638c\u63e1";
    private static final String STATUS_REVIEWING = "\u590d\u4e60\u4e2d";
    private static final String STATUS_MASTERED = "\u5df2\u638c\u63e1";
    private static final int[] EBBINGHAUS_INTERVAL_DAYS = {1, 2, 4, 7, 15, 30};

    private final WrongQuestionMapper wrongQuestionMapper;
    private final QuestionContentMapper questionContentMapper;
    private final QuestionImageMapper questionImageMapper;
    private final QuestionTagMapper questionTagMapper;
    private final TagMapper tagMapper;
    private final ReviewRecordMapper reviewRecordMapper;

    public ReviewServiceImpl(
            WrongQuestionMapper wrongQuestionMapper,
            QuestionContentMapper questionContentMapper,
            QuestionImageMapper questionImageMapper,
            QuestionTagMapper questionTagMapper,
            TagMapper tagMapper,
            ReviewRecordMapper reviewRecordMapper
    ) {
        this.wrongQuestionMapper = wrongQuestionMapper;
        this.questionContentMapper = questionContentMapper;
        this.questionImageMapper = questionImageMapper;
        this.questionTagMapper = questionTagMapper;
        this.tagMapper = tagMapper;
        this.reviewRecordMapper = reviewRecordMapper;
    }

    @Override
    public ReviewTodaySummaryVO getTodaySummary(Long userId) {
        syncUserReviewRecords(userId);

        List<Long> questionIds = listUserQuestionIds(userId);
        if (CollectionUtils.isEmpty(questionIds)) {
            ReviewTodaySummaryVO empty = new ReviewTodaySummaryVO();
            empty.setDueNowCount(0L);
            empty.setDueTodayCount(0L);
            empty.setOverdueCount(0L);
            empty.setDueLaterTodayCount(0L);
            empty.setDueNowPreview(new ArrayList<>());
            empty.setTodayPlanPreview(new ArrayList<>());
            empty.setTodayPreview(new ArrayList<>());
            return empty;
        }

        List<ReviewRecord> records = listReviewRecordsByQuestionIds(questionIds);
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);

        List<ReviewRecord> dueNowRecords = records.stream()
                .filter(record -> isDueNow(record.getNextReviewTime(), now))
                .sorted(compareByNextTime())
                .collect(Collectors.toList());

        List<ReviewRecord> todayPlanRecords = records.stream()
                .filter(record -> isTodayPlan(record.getNextReviewTime(), todayStart, endOfDay))
                .sorted(compareByNextTime())
                .collect(Collectors.toList());

        List<ReviewRecord> overdueRecords = records.stream()
                .filter(record -> isOverdue(record.getNextReviewTime(), todayStart))
                .sorted(compareByNextTime())
                .collect(Collectors.toList());

        List<ReviewRecord> dueLaterTodayRecords = records.stream()
                .filter(record -> isDueLaterToday(record.getNextReviewTime(), now, endOfDay))
                .sorted(compareByNextTime())
                .collect(Collectors.toList());

        LocalDateTime nextReviewTime = records.stream()
                .map(ReviewRecord::getNextReviewTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        if (nextReviewTime == null && records.stream().anyMatch(record -> record.getNextReviewTime() == null)) {
            nextReviewTime = now;
        }

        List<ReviewQuestionVO> dueNowPreview = buildReviewQuestions(extractQuestionIds(dueNowRecords, 5), userId);
        List<ReviewQuestionVO> todayPlanPreview = buildReviewQuestions(extractQuestionIds(todayPlanRecords, 5), userId);

        ReviewTodaySummaryVO summary = new ReviewTodaySummaryVO();
        summary.setDueNowCount((long) dueNowRecords.size());
        summary.setDueTodayCount((long) todayPlanRecords.size());
        summary.setOverdueCount((long) overdueRecords.size());
        summary.setDueLaterTodayCount((long) dueLaterTodayRecords.size());
        summary.setNextReviewTime(nextReviewTime);
        summary.setDueNowPreview(dueNowPreview);
        summary.setTodayPlanPreview(todayPlanPreview);
        summary.setTodayPreview(todayPlanPreview);
        return summary;
    }

    @Override
    public List<ReviewQuestionVO> listDueQuestions(Long userId) {
        return listDueNowQuestions(userId);
    }

    @Override
    public List<ReviewQuestionVO> listDueNowQuestions(Long userId) {
        syncUserReviewRecords(userId);

        List<Long> questionIds = listUserQuestionIds(userId);
        if (CollectionUtils.isEmpty(questionIds)) {
            return new ArrayList<>();
        }

        LocalDateTime now = LocalDateTime.now();
        List<Long> dueIds = listFilteredQuestionIds(questionIds, record -> isDueNow(record.getNextReviewTime(), now));

        return buildReviewQuestions(dueIds, userId);
    }

    @Override
    public List<ReviewQuestionVO> listTodayPlanQuestions(Long userId) {
        syncUserReviewRecords(userId);

        List<Long> questionIds = listUserQuestionIds(userId);
        if (CollectionUtils.isEmpty(questionIds)) {
            return new ArrayList<>();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);

        List<Long> todayPlanIds = listFilteredQuestionIds(
                questionIds,
                record -> isTodayPlan(record.getNextReviewTime(), todayStart, endOfDay)
        );

        return buildReviewQuestions(todayPlanIds, userId);
    }

    @Override
    public ReviewQuestionVO getNextQuestion(Long userId) {
        syncUserReviewRecords(userId);

        List<Long> questionIds = listUserQuestionIds(userId);
        if (CollectionUtils.isEmpty(questionIds)) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);

        List<Long> dueNowIds = listFilteredQuestionIds(questionIds, record -> isDueNow(record.getNextReviewTime(), now));
        if (!CollectionUtils.isEmpty(dueNowIds)) {
            List<ReviewQuestionVO> dueNowQuestions = buildReviewQuestions(Collections.singletonList(dueNowIds.get(0)), userId);
            if (!CollectionUtils.isEmpty(dueNowQuestions)) {
                return dueNowQuestions.get(0);
            }
        }

        List<Long> dueLaterTodayIds = listFilteredQuestionIds(
                questionIds,
                record -> isDueLaterToday(record.getNextReviewTime(), now, endOfDay)
        );
        if (!CollectionUtils.isEmpty(dueLaterTodayIds)) {
            List<ReviewQuestionVO> dueLaterTodayQuestions = buildReviewQuestions(
                    Collections.singletonList(dueLaterTodayIds.get(0)),
                    userId
            );
            if (!CollectionUtils.isEmpty(dueLaterTodayQuestions)) {
                return dueLaterTodayQuestions.get(0);
            }
        }

        List<Long> todayPlanIds = listFilteredQuestionIds(
                questionIds,
                record -> isTodayPlan(record.getNextReviewTime(), todayStart, endOfDay)
        );
        if (!CollectionUtils.isEmpty(todayPlanIds)) {
            List<ReviewQuestionVO> todayPlanQuestions = buildReviewQuestions(Collections.singletonList(todayPlanIds.get(0)), userId);
            if (!CollectionUtils.isEmpty(todayPlanQuestions)) {
                return todayPlanQuestions.get(0);
            }
        }

        List<Long> sortedIds = listReviewRecordsByQuestionIds(questionIds).stream()
                .sorted(compareByNextTime())
                .map(ReviewRecord::getQuestionId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sortedIds)) {
            return null;
        }

        List<ReviewQuestionVO> upcomingQuestions = buildReviewQuestions(Collections.singletonList(sortedIds.get(0)), userId);
        return CollectionUtils.isEmpty(upcomingQuestions) ? null : upcomingQuestions.get(0);
    }

    @Override
    public ReviewQuestionVO getQuestion(Long userId, Long questionId) {
        WrongQuestion question = getOwnQuestion(userId, questionId);
        List<ReviewQuestionVO> list = buildReviewQuestions(Collections.singletonList(question.getId()), userId);
        if (CollectionUtils.isEmpty(list)) {
            throw new RuntimeException("Question does not exist");
        }
        return list.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitReview(Long userId, Long questionId, ReviewSubmitDTO dto) {
        if (dto == null || dto.getCorrect() == null) {
            throw new RuntimeException("Review result is required");
        }

        WrongQuestion question = getOwnQuestion(userId, questionId);
        ReviewRecord record = getOrInitReviewRecord(question.getId(), question.getCreateTime());

        boolean correct = Boolean.TRUE.equals(dto.getCorrect());
        int reviewCount = safeNumber(record.getReviewCount());
        int masteryLevel = safeNumber(record.getMasteryLevel());

        if (correct) {
            reviewCount = Math.min(99, reviewCount + 1);
            masteryLevel = Math.min(5, masteryLevel + 1);
        } else {
            reviewCount = Math.max(0, reviewCount - 1);
            masteryLevel = Math.max(0, masteryLevel - 1);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReviewTime = calculateNextReviewTime(correct, reviewCount, now);

        record.setReviewCount(reviewCount);
        record.setMasteryLevel(masteryLevel);
        record.setLastReviewTime(now);
        record.setNextReviewTime(nextReviewTime);
        reviewRecordMapper.updateById(record);

        String newStatus = resolveStatusByMastery(masteryLevel);
        WrongQuestion update = new WrongQuestion();
        update.setId(question.getId());
        update.setStatus(newStatus);
        wrongQuestionMapper.updateById(update);

        ReviewQuestionVO questionVO = getQuestion(userId, questionId);

        Map<String, Object> result = new HashMap<>();
        result.put("correct", correct);
        result.put("reviewCount", reviewCount);
        result.put("masteryLevel", masteryLevel);
        result.put("nextReviewTime", nextReviewTime);
        result.put("status", newStatus);
        result.put("question", questionVO);
        return result;
    }

    private List<Long> listUserQuestionIds(Long userId) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getUserId, userId)
                .select(WrongQuestion::getId)
                .orderByAsc(WrongQuestion::getId);

        return wrongQuestionMapper.selectList(wrapper).stream()
                .map(WrongQuestion::getId)
                .collect(Collectors.toList());
    }

    private List<ReviewRecord> listReviewRecordsByQuestionIds(List<Long> questionIds) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<ReviewRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ReviewRecord::getQuestionId, questionIds)
                .orderByAsc(ReviewRecord::getId);
        return reviewRecordMapper.selectList(wrapper);
    }

    private List<Long> listFilteredQuestionIds(List<Long> questionIds, Predicate<ReviewRecord> predicate) {
        if (CollectionUtils.isEmpty(questionIds)) {
            return new ArrayList<>();
        }
        if (predicate == null) {
            return new ArrayList<>();
        }

        return listReviewRecordsByQuestionIds(questionIds).stream()
                .filter(predicate)
                .sorted(compareByNextTime())
                .map(ReviewRecord::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Long> extractQuestionIds(List<ReviewRecord> records, int limit) {
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }

        return records.stream()
                .limit(Math.max(0, limit))
                .map(ReviewRecord::getQuestionId)
                .collect(Collectors.toList());
    }

    private void syncUserReviewRecords(Long userId) {
        LambdaQueryWrapper<WrongQuestion> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(WrongQuestion::getUserId, userId)
                .select(WrongQuestion::getId, WrongQuestion::getCreateTime);
        List<WrongQuestion> questions = wrongQuestionMapper.selectList(questionWrapper);
        if (CollectionUtils.isEmpty(questions)) {
            return;
        }

        List<Long> questionIds = questions.stream().map(WrongQuestion::getId).collect(Collectors.toList());
        List<ReviewRecord> existingRecords = listReviewRecordsByQuestionIds(questionIds);
        Set<Long> existingQuestionIds = existingRecords.stream()
                .map(ReviewRecord::getQuestionId)
                .collect(Collectors.toCollection(HashSet::new));

        for (WrongQuestion question : questions) {
            if (existingQuestionIds.contains(question.getId())) {
                continue;
            }

            ReviewRecord record = new ReviewRecord();
            record.setQuestionId(question.getId());
            record.setReviewCount(0);
            record.setMasteryLevel(0);
            record.setNextReviewTime(question.getCreateTime() == null ? LocalDateTime.now() : question.getCreateTime());
            reviewRecordMapper.insert(record);
        }
    }

    private WrongQuestion getOwnQuestion(Long userId, Long questionId) {
        WrongQuestion question = wrongQuestionMapper.selectById(questionId);
        if (question == null) {
            throw new RuntimeException("Question does not exist");
        }
        if (!Objects.equals(userId, question.getUserId())) {
            throw new RuntimeException("No permission to access this question");
        }
        return question;
    }

    private ReviewRecord getOrInitReviewRecord(Long questionId, LocalDateTime questionCreateTime) {
        LambdaQueryWrapper<ReviewRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewRecord::getQuestionId, questionId)
                .orderByDesc(ReviewRecord::getId)
                .last("limit 1");

        ReviewRecord record = reviewRecordMapper.selectOne(wrapper);
        if (record != null) {
            return record;
        }

        ReviewRecord newRecord = new ReviewRecord();
        newRecord.setQuestionId(questionId);
        newRecord.setReviewCount(0);
        newRecord.setMasteryLevel(0);
        newRecord.setNextReviewTime(questionCreateTime == null ? LocalDateTime.now() : questionCreateTime);
        reviewRecordMapper.insert(newRecord);
        return newRecord;
    }

    private int safeNumber(Integer value) {
        return value == null ? 0 : value;
    }

    private LocalDateTime calculateNextReviewTime(boolean correct, int reviewCount, LocalDateTime now) {
        if (!correct) {
            return now.plusHours(12);
        }

        int index = Math.max(0, Math.min(EBBINGHAUS_INTERVAL_DAYS.length - 1, reviewCount - 1));
        int intervalDays = EBBINGHAUS_INTERVAL_DAYS[index];
        return now.plusDays(intervalDays);
    }

    private String resolveStatusByMastery(int masteryLevel) {
        if (masteryLevel >= 4) {
            return STATUS_MASTERED;
        }
        if (masteryLevel >= 2) {
            return STATUS_REVIEWING;
        }
        return STATUS_UNMASTERED;
    }

    private Comparator<ReviewRecord> compareByNextTime() {
        return Comparator
                .comparing(ReviewRecord::getNextReviewTime, Comparator.nullsFirst(LocalDateTime::compareTo))
                .thenComparing(ReviewRecord::getId, Comparator.nullsFirst(Long::compareTo));
    }

    private boolean isDueNow(LocalDateTime nextReviewTime, LocalDateTime now) {
        if (nextReviewTime == null) {
            return true;
        }
        return !nextReviewTime.isAfter(now);
    }

    private boolean isTodayPlan(LocalDateTime nextReviewTime, LocalDateTime todayStart, LocalDateTime endOfDay) {
        if (nextReviewTime == null) {
            return false;
        }
        if (nextReviewTime.isBefore(todayStart)) {
            return false;
        }
        return !nextReviewTime.isAfter(endOfDay);
    }

    private boolean isOverdue(LocalDateTime nextReviewTime, LocalDateTime todayStart) {
        if (nextReviewTime == null) {
            return true;
        }
        return nextReviewTime.isBefore(todayStart);
    }

    private boolean isDueLaterToday(LocalDateTime nextReviewTime, LocalDateTime now, LocalDateTime endOfDay) {
        if (nextReviewTime == null) {
            return false;
        }
        if (!nextReviewTime.isAfter(now)) {
            return false;
        }
        return !nextReviewTime.isAfter(endOfDay);
    }

    private List<ReviewQuestionVO> buildReviewQuestions(List<Long> orderedQuestionIds, Long userId) {
        if (CollectionUtils.isEmpty(orderedQuestionIds)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<WrongQuestion> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.in(WrongQuestion::getId, orderedQuestionIds)
                .eq(WrongQuestion::getUserId, userId);
        List<WrongQuestion> questions = wrongQuestionMapper.selectList(questionWrapper);
        Map<Long, WrongQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(WrongQuestion::getId, item -> item, (a, b) -> a));

        Map<Long, ReviewRecord> recordMap = new HashMap<>();
        List<ReviewRecord> records = listReviewRecordsByQuestionIds(orderedQuestionIds);
        for (ReviewRecord record : records) {
            recordMap.putIfAbsent(record.getQuestionId(), record);
        }

        Map<Long, QuestionContent> contentMap = new HashMap<>();
        LambdaQueryWrapper<QuestionContent> contentWrapper = new LambdaQueryWrapper<>();
        contentWrapper.in(QuestionContent::getQuestionId, orderedQuestionIds)
                .orderByDesc(QuestionContent::getId);
        List<QuestionContent> contentList = questionContentMapper.selectList(contentWrapper);
        for (QuestionContent content : contentList) {
            contentMap.putIfAbsent(content.getQuestionId(), content);
        }

        Map<Long, QuestionImage> imageMap = new HashMap<>();
        LambdaQueryWrapper<QuestionImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.in(QuestionImage::getQuestionId, orderedQuestionIds)
                .orderByDesc(QuestionImage::getId);
        List<QuestionImage> imageList = questionImageMapper.selectList(imageWrapper);
        for (QuestionImage image : imageList) {
            imageMap.putIfAbsent(image.getQuestionId(), image);
        }

        Map<Long, List<QuestionTagVO>> tagsMap = loadQuestionTags(orderedQuestionIds);

        List<ReviewQuestionVO> result = new ArrayList<>();
        for (Long questionId : orderedQuestionIds) {
            WrongQuestion question = questionMap.get(questionId);
            if (question == null) {
                continue;
            }

            ReviewQuestionVO vo = new ReviewQuestionVO();
            vo.setQuestionId(question.getId());
            vo.setSubject(question.getSubject());
            vo.setKnowledgePoint(question.getKnowledgePoint());
            vo.setErrorReason(question.getErrorReason());
            vo.setDifficulty(question.getDifficulty());
            vo.setStatus(question.getStatus());

            QuestionContent content = contentMap.get(questionId);
            if (content != null) {
                vo.setContent(content.getContent());
                vo.setAnswer(content.getAnswer());
                vo.setAnalysis(content.getAnalysis());
            }

            QuestionImage image = imageMap.get(questionId);
            if (image != null) {
                vo.setImageUrl(image.getImageUrl());
            }

            ReviewRecord record = recordMap.get(questionId);
            if (record != null) {
                vo.setReviewCount(record.getReviewCount());
                vo.setMasteryLevel(record.getMasteryLevel());
                vo.setLastReviewTime(record.getLastReviewTime());
                vo.setNextReviewTime(record.getNextReviewTime());
            } else {
                vo.setReviewCount(0);
                vo.setMasteryLevel(0);
            }

            vo.setTags(tagsMap.getOrDefault(questionId, new ArrayList<>()));
            result.add(vo);
        }

        return result;
    }

    private Map<Long, List<QuestionTagVO>> loadQuestionTags(List<Long> questionIds) {
        Map<Long, List<QuestionTagVO>> result = new HashMap<>();
        if (CollectionUtils.isEmpty(questionIds)) {
            return result;
        }

        LambdaQueryWrapper<QuestionTag> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.in(QuestionTag::getQuestionId, questionIds)
                .orderByAsc(QuestionTag::getId);
        List<QuestionTag> relations = questionTagMapper.selectList(relationWrapper);
        if (CollectionUtils.isEmpty(relations)) {
            return result;
        }

        Set<Long> tagIds = relations.stream()
                .map(QuestionTag::getTagId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        if (CollectionUtils.isEmpty(tagIds)) {
            return result;
        }

        LambdaQueryWrapper<Tag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.in(Tag::getId, tagIds);
        List<Tag> tags = tagMapper.selectList(tagWrapper);
        Map<Long, Tag> tagMap = tags.stream().collect(Collectors.toMap(Tag::getId, t -> t, (a, b) -> a));

        for (QuestionTag relation : relations) {
            Tag tag = tagMap.get(relation.getTagId());
            if (tag == null) {
                continue;
            }

            List<QuestionTagVO> list = result.computeIfAbsent(relation.getQuestionId(), key -> new ArrayList<>());
            QuestionTagVO vo = new QuestionTagVO();
            vo.setId(tag.getId());
            vo.setName(tag.getName());
            list.add(vo);
        }
        return result;
    }
}
