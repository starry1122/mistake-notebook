package com.wsy.mistake_notebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wsy.mistake_notebook.config.MistakeModuleProperties;
import com.wsy.mistake_notebook.dto.ImageWrongQuestionCreateDTO;
import com.wsy.mistake_notebook.dto.ManualWrongQuestionCreateDTO;
import com.wsy.mistake_notebook.dto.QuestionTagsUpdateDTO;
import com.wsy.mistake_notebook.dto.WrongQuestionQueryDTO;
import com.wsy.mistake_notebook.dto.WrongQuestionUpdateDTO;
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
import com.wsy.mistake_notebook.service.WrongQuestionService;
import com.wsy.mistake_notebook.vo.QuestionTagVO;
import com.wsy.mistake_notebook.vo.WrongQuestionManageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class WrongQuestionServiceImpl implements WrongQuestionService {

    private final WrongQuestionMapper wrongQuestionMapper;
    private final QuestionContentMapper questionContentMapper;
    private final QuestionImageMapper questionImageMapper;
    private final QuestionTagMapper questionTagMapper;
    private final TagMapper tagMapper;
    private final ReviewRecordMapper reviewRecordMapper;
    private final MistakeModuleProperties properties;

    public WrongQuestionServiceImpl(
            WrongQuestionMapper wrongQuestionMapper,
            QuestionContentMapper questionContentMapper,
            QuestionImageMapper questionImageMapper,
            QuestionTagMapper questionTagMapper,
            ReviewRecordMapper reviewRecordMapper,
            TagMapper tagMapper,
            MistakeModuleProperties properties
    ) {
        this.wrongQuestionMapper = wrongQuestionMapper;
        this.questionContentMapper = questionContentMapper;
        this.questionImageMapper = questionImageMapper;
        this.questionTagMapper = questionTagMapper;
        this.reviewRecordMapper = reviewRecordMapper;
        this.tagMapper = tagMapper;
        this.properties = properties;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createManual(Long userId, ManualWrongQuestionCreateDTO dto) {
        if (!StringUtils.hasText(dto.getContent())) {
            throw new RuntimeException("Question content cannot be empty");
        }

        WrongQuestion wrongQuestion = buildWrongQuestion(
                userId,
                dto.getSubject(),
                dto.getKnowledgePoint(),
                "manual",
                dto.getErrorReason(),
                dto.getDifficulty(),
                dto.getFavorite()
        );
        wrongQuestionMapper.insert(wrongQuestion);
        initReviewRecord(wrongQuestion.getId(), wrongQuestion.getCreateTime());

        QuestionContent questionContent = new QuestionContent();
        questionContent.setQuestionId(wrongQuestion.getId());
        questionContent.setContentType("manual");
        questionContent.setContent(dto.getContent());
        questionContent.setAnswer(dto.getAnswer());
        questionContent.setAnalysis(dto.getAnalysis());
        questionContentMapper.insert(questionContent);

        Set<Long> tagIds = resolveTagIds(userId, dto.getTagIds(), dto.getTagNames());
        bindQuestionTags(wrongQuestion.getId(), tagIds);

        Map<String, Object> result = new HashMap<>();
        result.put("questionId", wrongQuestion.getId());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createImage(Long userId, ImageWrongQuestionCreateDTO dto, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please upload an image file");
        }

        WrongQuestion wrongQuestion = buildWrongQuestion(
                userId,
                dto.getSubject(),
                dto.getKnowledgePoint(),
                "image",
                dto.getErrorReason(),
                dto.getDifficulty(),
                dto.getFavorite()
        );
        wrongQuestionMapper.insert(wrongQuestion);
        initReviewRecord(wrongQuestion.getId(), wrongQuestion.getCreateTime());

        String imageUrl = saveUploadFile(file, userId);

        OcrResult ocrResult = resolveOcr(file);

        QuestionImage questionImage = new QuestionImage();
        questionImage.setQuestionId(wrongQuestion.getId());
        questionImage.setImageUrl(imageUrl);
        questionImage.setOcrStatus(ocrResult.status());
        questionImage.setOcrText(ocrResult.text());
        questionImageMapper.insert(questionImage);

        QuestionContent questionContent = new QuestionContent();
        questionContent.setQuestionId(wrongQuestion.getId());
        questionContent.setContentType("ocr");
        questionContent.setContent(ocrResult.text());
        questionContentMapper.insert(questionContent);

        Set<Long> tagIds = resolveTagIds(userId, dto.getTagIds(), dto.getTagNames());
        bindQuestionTags(wrongQuestion.getId(), tagIds);

        Map<String, Object> result = new HashMap<>();
        result.put("questionId", wrongQuestion.getId());
        result.put("imageUrl", imageUrl);
        result.put("ocrStatus", ocrResult.status());
        result.put("ocrText", ocrResult.text());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestionTags(Long userId, Long questionId, QuestionTagsUpdateDTO dto) {
        ensureOwnQuestion(userId, questionId);

        List<Long> requestTagIds = dto == null ? null : dto.getTagIds();
        List<String> requestTagNames = dto == null ? null : dto.getTagNames();
        Set<Long> tagIds = resolveTagIds(userId, requestTagIds, requestTagNames);
        replaceQuestionTags(questionId, tagIds);
    }

    @Override
    public Map<String, Object> listByCondition(Long userId, WrongQuestionQueryDTO dto) {
        WrongQuestionQueryDTO query = dto == null ? new WrongQuestionQueryDTO() : dto;
        long pageNo = safePageNo(query.getPageNo());
        long pageSize = safePageSize(query.getPageSize());

        Page<WrongQuestion> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<WrongQuestion> wrapper = buildFilterWrapper(userId, query);
        wrapper.orderByDesc(WrongQuestion::getUpdateTime)
                .orderByDesc(WrongQuestion::getId);

        Page<WrongQuestion> resultPage = wrongQuestionMapper.selectPage(page, wrapper);
        List<WrongQuestionManageVO> records = buildManageVOList(resultPage.getRecords());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", resultPage.getTotal());
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public WrongQuestionManageVO getDetail(Long userId, Long questionId) {
        WrongQuestion question = ensureOwnQuestion(userId, questionId);
        List<WrongQuestionManageVO> list = buildManageVOList(Collections.singletonList(question));
        if (list.isEmpty()) {
            throw new RuntimeException("Wrong question does not exist");
        }
        return list.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestion(Long userId, Long questionId, WrongQuestionUpdateDTO dto) {
        ensureOwnQuestion(userId, questionId);
        if (dto == null) {
            return;
        }

        LambdaUpdateWrapper<WrongQuestion> questionUpdate = new LambdaUpdateWrapper<>();
        questionUpdate.eq(WrongQuestion::getId, questionId)
                .eq(WrongQuestion::getUserId, userId);

        boolean needUpdateQuestion = false;
        if (dto.getSubject() != null) {
            questionUpdate.set(WrongQuestion::getSubject, trimToNull(dto.getSubject()));
            needUpdateQuestion = true;
        }
        if (dto.getKnowledgePoint() != null) {
            questionUpdate.set(WrongQuestion::getKnowledgePoint, trimToNull(dto.getKnowledgePoint()));
            needUpdateQuestion = true;
        }
        if (dto.getErrorReason() != null) {
            questionUpdate.set(WrongQuestion::getErrorReason, trimToNull(dto.getErrorReason()));
            needUpdateQuestion = true;
        }
        if (dto.getDifficulty() != null) {
            questionUpdate.set(WrongQuestion::getDifficulty, dto.getDifficulty());
            needUpdateQuestion = true;
        }
        if (dto.getStatus() != null) {
            questionUpdate.set(WrongQuestion::getStatus, trimToNull(dto.getStatus()));
            needUpdateQuestion = true;
        }
        if (dto.getFavorite() != null) {
            questionUpdate.set(WrongQuestion::getFavorite, Boolean.TRUE.equals(dto.getFavorite()));
            needUpdateQuestion = true;
        }
        if (needUpdateQuestion) {
            wrongQuestionMapper.update(null, questionUpdate);
        }

        boolean hasContentPayload = dto.getContent() != null || dto.getAnswer() != null || dto.getAnalysis() != null;
        if (hasContentPayload) {
            LambdaQueryWrapper<QuestionContent> contentWrapper = new LambdaQueryWrapper<>();
            contentWrapper.eq(QuestionContent::getQuestionId, questionId)
                    .orderByDesc(QuestionContent::getId)
                    .last("limit 1");
            QuestionContent existingContent = questionContentMapper.selectOne(contentWrapper);

            if (existingContent == null) {
                QuestionContent content = new QuestionContent();
                content.setQuestionId(questionId);
                content.setContentType("manual");
                content.setContent(trimToNull(dto.getContent()));
                content.setAnswer(trimToNull(dto.getAnswer()));
                content.setAnalysis(trimToNull(dto.getAnalysis()));
                questionContentMapper.insert(content);
            } else {
                LambdaUpdateWrapper<QuestionContent> contentUpdate = new LambdaUpdateWrapper<>();
                contentUpdate.eq(QuestionContent::getId, existingContent.getId());
                if (dto.getContent() != null) {
                    contentUpdate.set(QuestionContent::getContent, trimToNull(dto.getContent()));
                }
                if (dto.getAnswer() != null) {
                    contentUpdate.set(QuestionContent::getAnswer, trimToNull(dto.getAnswer()));
                }
                if (dto.getAnalysis() != null) {
                    contentUpdate.set(QuestionContent::getAnalysis, trimToNull(dto.getAnalysis()));
                }
                questionContentMapper.update(null, contentUpdate);
            }
        }

        if (dto.getTagIds() != null || dto.getTagNames() != null) {
            Set<Long> tagIds = resolveTagIds(userId, dto.getTagIds(), dto.getTagNames());
            replaceQuestionTags(questionId, tagIds);
        }
    }

    @Override
    public void deleteQuestion(Long userId, Long questionId) {
        ensureOwnQuestion(userId, questionId);
        wrongQuestionMapper.deleteById(questionId);
    }

    @Override
    public void updateFavorite(Long userId, Long questionId, Boolean favorite) {
        if (favorite == null) {
            throw new RuntimeException("Favorite flag is required");
        }

        LambdaUpdateWrapper<WrongQuestion> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WrongQuestion::getId, questionId)
                .eq(WrongQuestion::getUserId, userId)
                .set(WrongQuestion::getFavorite, Boolean.TRUE.equals(favorite));

        int affected = wrongQuestionMapper.update(null, updateWrapper);
        if (affected == 0) {
            throw new RuntimeException("Wrong question does not exist");
        }
    }

    @Override
    public List<String> listSubjects(Long userId) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getUserId, userId)
                .select(WrongQuestion::getSubject)
                .orderByAsc(WrongQuestion::getSubject);

        List<WrongQuestion> rows = wrongQuestionMapper.selectList(wrapper);
        return rows.stream()
                .map(WrongQuestion::getSubject)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listKnowledgePoints(Long userId, String subject) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getUserId, userId)
                .select(WrongQuestion::getKnowledgePoint)
                .orderByAsc(WrongQuestion::getKnowledgePoint);
        if (StringUtils.hasText(subject)) {
            wrapper.eq(WrongQuestion::getSubject, trimToNull(subject));
        }

        List<WrongQuestion> rows = wrongQuestionMapper.selectList(wrapper);
        return rows.stream()
                .map(WrongQuestion::getKnowledgePoint)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }

    private WrongQuestion buildWrongQuestion(
            Long userId,
            String subject,
            String knowledgePoint,
            String sourceType,
            String errorReason,
            Integer difficulty,
            Boolean favorite
    ) {
        WrongQuestion wrongQuestion = new WrongQuestion();
        wrongQuestion.setUserId(userId);
        wrongQuestion.setSubject(trimToNull(subject));
        wrongQuestion.setKnowledgePoint(trimToNull(knowledgePoint));
        wrongQuestion.setSourceType(sourceType);
        wrongQuestion.setErrorReason(trimToNull(errorReason));
        wrongQuestion.setDifficulty(difficulty);
        wrongQuestion.setFavorite(Boolean.TRUE.equals(favorite));
        return wrongQuestion;
    }

    private WrongQuestion ensureOwnQuestion(Long userId, Long questionId) {
        WrongQuestion question = wrongQuestionMapper.selectById(questionId);
        if (question == null) {
            throw new RuntimeException("Wrong question does not exist");
        }
        if (!Objects.equals(userId, question.getUserId())) {
            throw new RuntimeException("No permission to modify this question");
        }
        return question;
    }

    private LambdaQueryWrapper<WrongQuestion> buildFilterWrapper(Long userId, WrongQuestionQueryDTO dto) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getUserId, userId);

        if (StringUtils.hasText(dto.getSubject())) {
            wrapper.eq(WrongQuestion::getSubject, trimToNull(dto.getSubject()));
        }
        if (StringUtils.hasText(dto.getKnowledgePoint())) {
            wrapper.like(WrongQuestion::getKnowledgePoint, trimToNull(dto.getKnowledgePoint()));
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(WrongQuestion::getStatus, trimToNull(dto.getStatus()));
        }
        if (dto.getDifficulty() != null) {
            wrapper.eq(WrongQuestion::getDifficulty, dto.getDifficulty());
        }
        if (dto.getFavorite() != null) {
            wrapper.eq(WrongQuestion::getFavorite, dto.getFavorite());
        }
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();
        if (startTime != null) {
            wrapper.ge(WrongQuestion::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(WrongQuestion::getCreateTime, endTime);
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            String keyword = trimToNull(dto.getKeyword());
            wrapper.and(w -> w.like(WrongQuestion::getSubject, keyword)
                    .or()
                    .like(WrongQuestion::getKnowledgePoint, keyword)
                    .or()
                    .like(WrongQuestion::getErrorReason, keyword));
        }
        return wrapper;
    }

    private long safePageNo(Integer pageNo) {
        if (pageNo == null || pageNo < 1) {
            return 1L;
        }
        return pageNo;
    }

    private long safePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10L;
        }
        return Math.min(pageSize, 50);
    }

    private List<WrongQuestionManageVO> buildManageVOList(List<WrongQuestion> questions) {
        if (CollectionUtils.isEmpty(questions)) {
            return new ArrayList<>();
        }

        List<Long> questionIds = questions.stream().map(WrongQuestion::getId).collect(Collectors.toList());

        Map<Long, QuestionContent> contentMap = new HashMap<>();
        LambdaQueryWrapper<QuestionContent> contentWrapper = new LambdaQueryWrapper<>();
        contentWrapper.in(QuestionContent::getQuestionId, questionIds)
                .orderByDesc(QuestionContent::getId);
        List<QuestionContent> contentList = questionContentMapper.selectList(contentWrapper);
        for (QuestionContent item : contentList) {
            contentMap.putIfAbsent(item.getQuestionId(), item);
        }

        Map<Long, QuestionImage> imageMap = new HashMap<>();
        LambdaQueryWrapper<QuestionImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.in(QuestionImage::getQuestionId, questionIds)
                .orderByDesc(QuestionImage::getId);
        List<QuestionImage> imageList = questionImageMapper.selectList(imageWrapper);
        for (QuestionImage item : imageList) {
            imageMap.putIfAbsent(item.getQuestionId(), item);
        }

        Map<Long, List<QuestionTagVO>> tagsMap = loadQuestionTags(questionIds);

        List<WrongQuestionManageVO> result = new ArrayList<>();
        for (WrongQuestion question : questions) {
            WrongQuestionManageVO vo = new WrongQuestionManageVO();
            vo.setId(question.getId());
            vo.setSubject(question.getSubject());
            vo.setKnowledgePoint(question.getKnowledgePoint());
            vo.setSourceType(question.getSourceType());
            vo.setErrorReason(question.getErrorReason());
            vo.setDifficulty(question.getDifficulty());
            vo.setStatus(question.getStatus());
            vo.setFavorite(Boolean.TRUE.equals(question.getFavorite()));
            vo.setCreateTime(question.getCreateTime());
            vo.setUpdateTime(question.getUpdateTime());

            QuestionContent content = contentMap.get(question.getId());
            if (content != null) {
                vo.setContent(content.getContent());
                vo.setAnswer(content.getAnswer());
                vo.setAnalysis(content.getAnalysis());
            }

            QuestionImage image = imageMap.get(question.getId());
            if (image != null) {
                vo.setImageUrl(image.getImageUrl());
                vo.setOcrStatus(image.getOcrStatus());
                vo.setOcrText(image.getOcrText());
            }

            List<QuestionTagVO> tags = tagsMap.get(question.getId());
            vo.setTags(tags == null ? new ArrayList<>() : tags);
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

    private void replaceQuestionTags(Long questionId, Set<Long> tagIds) {
        LambdaQueryWrapper<QuestionTag> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(QuestionTag::getQuestionId, questionId);
        questionTagMapper.delete(deleteWrapper);
        bindQuestionTags(questionId, tagIds);
    }

    private void bindQuestionTags(Long questionId, Set<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }

        for (Long tagId : tagIds) {
            QuestionTag questionTag = new QuestionTag();
            questionTag.setQuestionId(questionId);
            questionTag.setTagId(tagId);
            questionTagMapper.insert(questionTag);
        }
    }

    private void initReviewRecord(Long questionId, LocalDateTime questionCreateTime) {
        if (questionId == null) {
            return;
        }

        LambdaQueryWrapper<ReviewRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewRecord::getQuestionId, questionId)
                .last("limit 1");

        ReviewRecord existing = reviewRecordMapper.selectOne(wrapper);
        if (existing != null) {
            return;
        }

        ReviewRecord record = new ReviewRecord();
        record.setQuestionId(questionId);
        record.setReviewCount(0);
        record.setMasteryLevel(0);
        record.setNextReviewTime(questionCreateTime == null ? LocalDateTime.now() : questionCreateTime);
        reviewRecordMapper.insert(record);
    }

    private Set<Long> resolveTagIds(Long userId, List<Long> tagIds, List<String> tagNames) {
        LinkedHashSet<Long> resolved = new LinkedHashSet<>();

        if (!CollectionUtils.isEmpty(tagIds)) {
            Set<Long> requestIds = tagIds.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (!requestIds.isEmpty()) {
                LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Tag::getUserId, userId)
                        .in(Tag::getId, requestIds);
                List<Tag> ownTags = tagMapper.selectList(wrapper);
                Set<Long> ownTagIds = ownTags.stream()
                        .map(Tag::getId)
                        .collect(Collectors.toCollection(HashSet::new));

                if (ownTagIds.size() != requestIds.size()) {
                    throw new RuntimeException("Invalid tag id or non-owner tag detected");
                }
                resolved.addAll(ownTagIds);
            }
        }

        if (!CollectionUtils.isEmpty(tagNames)) {
            for (String name : tagNames) {
                String tagName = trimToNull(name);
                if (!StringUtils.hasText(tagName)) {
                    continue;
                }

                LambdaQueryWrapper<Tag> query = new LambdaQueryWrapper<>();
                query.eq(Tag::getUserId, userId)
                        .eq(Tag::getName, tagName);
                Tag existing = tagMapper.selectOne(query);
                if (existing != null) {
                    resolved.add(existing.getId());
                    continue;
                }

                Tag tag = new Tag();
                tag.setUserId(userId);
                tag.setName(tagName);
                tagMapper.insert(tag);
                resolved.add(tag.getId());
            }
        }

        return resolved;
    }

    private String saveUploadFile(MultipartFile file, Long userId) {
        String uploadDir = properties.getUploadDir();
        if (!StringUtils.hasText(uploadDir)) {
            uploadDir = "uploads";
        }

        Path uploadPath = Path.of(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadPath);

            String originalName = file.getOriginalFilename();
            String extension = StringUtils.getFilenameExtension(originalName);

            String fileName = userId + "_"
                    + System.currentTimeMillis() + "_"
                    + UUID.randomUUID().toString().replace("-", "");

            if (StringUtils.hasText(extension)) {
                fileName = fileName + "." + extension;
            }

            Path target = uploadPath.resolve(fileName);
            file.transferTo(target);
            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image", e);
        }
    }

    private OcrResult resolveOcr(MultipartFile file) {
        String mode = properties.getOcr() == null ? null : properties.getOcr().getMode();
        String normalizedMode = trimToNull(mode);

        if ("off".equalsIgnoreCase(normalizedMode)) {
            return new OcrResult("pending", null);
        }

        if ("tesseract".equalsIgnoreCase(normalizedMode)) {
            return resolveByTesseract(file);
        }

        String filename = trimToNull(file.getOriginalFilename());
        if (!StringUtils.hasText(filename)) {
            filename = "image";
        }

        String text = "Mock OCR text: " + filename;
        return new OcrResult("success", text);
    }

    private OcrResult resolveByTesseract(MultipartFile file) {
        String tesseractCmd = properties.getOcr() == null ? null : properties.getOcr().getTesseractCmd();
        String lang = properties.getOcr() == null ? null : properties.getOcr().getLang();

        String normalizedCmd = StringUtils.hasText(tesseractCmd) ? tesseractCmd.trim() : "tesseract";
        String normalizedLang = StringUtils.hasText(lang) ? lang.trim() : "chi_sim+eng";

        Path tempDir = null;
        Path tempImage = null;
        Path outputBase = null;
        Path outputTxt = null;
        Process process = null;

        try {
            tempDir = Files.createTempDirectory("mistake_ocr_");

            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String suffix = StringUtils.hasText(extension) ? "." + extension : ".png";

            tempImage = Files.createTempFile(tempDir, "input_", suffix);
            file.transferTo(tempImage);

            outputBase = tempDir.resolve("ocr_result");
            outputTxt = tempDir.resolve("ocr_result.txt");

            ProcessBuilder builder = new ProcessBuilder(
                    normalizedCmd,
                    tempImage.toString(),
                    outputBase.toString(),
                    "-l",
                    normalizedLang
            );
            builder.redirectErrorStream(true);

            process = builder.start();

            StringBuilder outputLogBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (outputLogBuilder.length() > 0) {
                        outputLogBuilder.append('\n');
                    }
                    outputLogBuilder.append(line);
                }
            }

            boolean finished = process.waitFor(20, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return buildOcrFallback(file, "tesseract execution timeout");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String outputLog = trimToNull(outputLogBuilder.toString());
                String reason = StringUtils.hasText(outputLog)
                        ? "exitCode=" + exitCode + ", output=" + outputLog
                        : "exitCode=" + exitCode;
                return buildOcrFallback(file, reason);
            }

            if (!Files.exists(outputTxt)) {
                return buildOcrFallback(file, "tesseract output file missing");
            }

            String recognizedText = Files.readString(outputTxt, StandardCharsets.UTF_8);
            String normalizedText = trimToNull(recognizedText);
            if (!StringUtils.hasText(normalizedText)) {
                return buildOcrFallback(file, "tesseract output is empty");
            }

            String finalText = "\u3010\u672c\u5730Tesseract\u8bc6\u522b\u7ed3\u679c\uff08\u53ef\u80fd\u5b58\u5728\u8bef\u5dee\uff09\u3011\n" + normalizedText;
            return new OcrResult("success", finalText);
        } catch (Exception e) {
            return buildOcrFallback(file, e.getMessage());
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            deleteTempDirQuietly(tempDir);
        }
    }

    private OcrResult buildOcrFallback(MultipartFile file, String reason) {
        String filename = trimToNull(file.getOriginalFilename());
        if (!StringUtils.hasText(filename)) {
            filename = "image";
        }

        String normalizedReason = StringUtils.hasText(reason) ? reason : "unknown error";
        String text = "\u3010Tesseract\u8bc6\u522b\u5931\u8d25\uff0c\u5df2\u56de\u9000Mock\u7ed3\u679c\u3011\n"
                + "\u5931\u8d25\u539f\u56e0\uff1a" + normalizedReason + "\n"
                + "Mock OCR text: " + filename + "\n"
                + "\uff08\u63d0\u793a\uff1a\u672c\u5730OCR\u8bc6\u522b\u53ef\u80fd\u5b58\u5728\u8bef\u5dee\uff09";
        return new OcrResult("failed", text);
    }

    private void deleteTempDirQuietly(Path tempDir) {
        if (tempDir == null) {
            return;
        }

        try {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static class OcrResult {
        private final String status;
        private final String text;

        private OcrResult(String status, String text) {
            this.status = status;
            this.text = text;
        }

        private String status() {
            return status;
        }

        private String text() {
            return text;
        }
    }
}
