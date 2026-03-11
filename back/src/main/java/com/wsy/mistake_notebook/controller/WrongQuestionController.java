package com.wsy.mistake_notebook.controller;

import com.wsy.mistake_notebook.dto.ImageWrongQuestionCreateDTO;
import com.wsy.mistake_notebook.dto.ManualWrongQuestionCreateDTO;
import com.wsy.mistake_notebook.dto.QuestionTagsUpdateDTO;
import com.wsy.mistake_notebook.dto.WrongQuestionQueryDTO;
import com.wsy.mistake_notebook.dto.WrongQuestionUpdateDTO;
import com.wsy.mistake_notebook.service.WrongQuestionService;
import com.wsy.mistake_notebook.utils.AuthUtil;
import com.wsy.mistake_notebook.vo.WrongQuestionManageVO;
import com.wsy.mistake_notebook.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/student/questions")
public class WrongQuestionController {

    private final WrongQuestionService wrongQuestionService;

    public WrongQuestionController(WrongQuestionService wrongQuestionService) {
        this.wrongQuestionService = wrongQuestionService;
    }

    @GetMapping
    public Result<Map<String, Object>> listQuestions(
            @ModelAttribute WrongQuestionQueryDTO dto,
            HttpServletRequest request
    ) {
        Long userId = AuthUtil.requireStudentUserId(request);
        Map<String, Object> result = wrongQuestionService.listByCondition(userId, dto);

        Object recordsObj = result.get("records");
        if (recordsObj instanceof List<?>) {
            List<?> list = (List<?>) recordsObj;
            for (Object item : list) {
                if (item instanceof WrongQuestionManageVO) {
                    WrongQuestionManageVO vo = (WrongQuestionManageVO) item;
                    vo.setImageUrl(toAbsoluteUrl(request, vo.getImageUrl()));
                }
            }
        }
        return Result.success(result);
    }

    @GetMapping("/subjects")
    public Result<List<String>> listSubjects(HttpServletRequest request) {
        Long userId = AuthUtil.requireStudentUserId(request);
        return Result.success(wrongQuestionService.listSubjects(userId));
    }

    @GetMapping("/knowledge-points")
    public Result<List<String>> listKnowledgePoints(
            @RequestParam(required = false) String subject,
            HttpServletRequest request
    ) {
        Long userId = AuthUtil.requireStudentUserId(request);
        return Result.success(wrongQuestionService.listKnowledgePoints(userId, subject));
    }

    @GetMapping("/{questionId:[0-9]+}")
    public Result<WrongQuestionManageVO> getDetail(
            @PathVariable Long questionId,
            HttpServletRequest request
    ) {
        Long userId = AuthUtil.requireStudentUserId(request);
        WrongQuestionManageVO vo = wrongQuestionService.getDetail(userId, questionId);
        vo.setImageUrl(toAbsoluteUrl(request, vo.getImageUrl()));
        return Result.success(vo);
    }

    @PostMapping("/manual")
    public Result<Map<String, Object>> createManual(
            @Valid @RequestBody ManualWrongQuestionCreateDTO dto,
            HttpServletRequest request
    ) {
        Long userId = AuthUtil.requireStudentUserId(request);
        return Result.success(wrongQuestionService.createManual(userId, dto));
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, Object>> createImage(
            @RequestPart("file") MultipartFile file,
            @Valid @RequestPart("meta") ImageWrongQuestionCreateDTO dto,
            HttpServletRequest request
    ) {
        Long userId = AuthUtil.requireStudentUserId(request);
        Map<String, Object> result = wrongQuestionService.createImage(userId, dto, file);

        Object imageUrlObj = result.get("imageUrl");
        if (imageUrlObj instanceof String) {
            String imageUrl = (String) imageUrlObj;
            if (imageUrl.startsWith("/")) {
                String fullUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + imageUrl;
                result.put("imageUrl", fullUrl);
            }
        }

        return Result.success(result);
    }

    @PutMapping("/{questionId:[0-9]+}")
    public Result<String> updateQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody WrongQuestionUpdateDTO dto,
            HttpServletRequest request
    ) {
        Long userId = AuthUtil.requireStudentUserId(request);
        wrongQuestionService.updateQuestion(userId, questionId, dto);
        return Result.success("Updated");
    }

    @DeleteMapping("/{questionId:[0-9]+}")
    public Result<String> deleteQuestion(
            @PathVariable Long questionId,
            HttpServletRequest request
    ) {
        Long userId = AuthUtil.requireStudentUserId(request);
        wrongQuestionService.deleteQuestion(userId, questionId);
        return Result.success("Deleted");
    }

    @PutMapping("/{questionId:[0-9]+}/favorite")
    public Result<String> updateFavorite(
            @PathVariable Long questionId,
            @RequestParam Boolean favorite,
            HttpServletRequest request
    ) {
        Long userId = AuthUtil.requireStudentUserId(request);
        wrongQuestionService.updateFavorite(userId, questionId, favorite);
        return Result.success("Updated");
    }

    @PutMapping("/{questionId:[0-9]+}/tags")
    public Result<String> updateQuestionTags(
            @PathVariable Long questionId,
            @RequestBody QuestionTagsUpdateDTO dto,
            HttpServletRequest request
    ) {
        Long userId = AuthUtil.requireStudentUserId(request);
        wrongQuestionService.updateQuestionTags(userId, questionId, dto);
        return Result.success("Updated");
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
